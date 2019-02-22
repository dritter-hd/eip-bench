/*******************************************************************************
 * Copyright (c) 2011, Chair of Distributed Information Systems, University of Passau. 
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *     this list of conditions and the following disclaimer. 
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *     notice, this list of conditions and the following disclaimer in the 
 *     documentation and/or other materials provided with the distribution. 
 * 
 * 3. Neither the name of the University of Passau nor the names of its 
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 * DAMAGE.
 ******************************************************************************/
package pdgf.core.dataGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;

import org.slf4j.LoggerFactory;
import pdgf.Controller;
import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.scheduler.Scheduler;
import pdgf.core.dbSchema.Field;
import pdgf.core.dbSchema.Project;
import pdgf.core.dbSchema.Table;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.output.FileOutput;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.util.Constants;

/**
 * DataGeneration controller class
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class DataGenerator extends Observable {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(DataGenerator.class);

	private Worker[] workers = null;
	private boolean successfullyInitialized = false;
	private Project p = null;
	private Controller c = null;
	private boolean closeAfterFinish = false;
	private Scheduler scheduler = null;

	private long starttime;
	private long endtime = Constants.LONG_NOT_SET;

	private boolean started = false;

	private boolean successfulFinished = false;

	/**
	 * uses this datagenerator without a controller
	 */
	public DataGenerator() {
	}

	/**
	 * uses this datagenerator with a controller
	 */
	public DataGenerator(Controller c) {
		this.c = c;
	}

	/**
	 * Start data generation process. Generator must be initialized with
	 * {@linkplain DataGenerator#initialize(Project)} before.
	 * 
	 * @param p
	 *            The project
	 * @return true if start of generation process was successful
	 * @throws XmlException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public synchronized boolean start() throws IOException, XmlException,
			ConfigurationException {

		if (!successfullyInitialized) {
			throw new ConfigurationException(
					"Data Generator is not initialized! This means some configuration files are missing.");
		}

		if (started) {
			echoToObservers("Generator allready started! Stop first before calling start again.");
		} else {
			successfulFinished = false;
			starttime = System.currentTimeMillis();
			scheduler.start();
			// start datageneration
			for (int i = 0; i < workers.length; i++) {

				workers[i].start();
			}

			String msg = "Data generation procces started. Created "
					+ workers.length + " workers for this task.";
			log.info(msg);
			echoToObservers(msg);
			started = true;
			return true;
		}
		return false;
	}

	/**
	 * stops datageneration on all workers, forcing threads to die!
	 */
	public synchronized void stop() {
		if (started) {
			started = false;

			if (successfulFinished) {
				generateReport();
			}

			if (scheduler != null) {
				scheduler.stop();
			}
			if (workers != null) {
				for (int i = 0; i < workers.length; i++) {
					if (workers[i] != null) {
						log.debug("sending stop command to worker " + (i + 1)
								+ " was alive? " + workers[i].isAlive());
						workers[i].stopWorker();
						log.debug("stop successful, wait to die...");
						long start = System.currentTimeMillis();
						try {
							workers[i].join(100);// wait 100ms
						} catch (InterruptedException e) {

						}
						if (System.currentTimeMillis() - start > 100) {
							log.info("stop failed for worker: " + (i + 1)
									+ " force worker to die..");
							try {
								workers[i].interrupt();
								workers[i].join(100);// wait 100ms
							} catch (InterruptedException e) {

							} catch (SecurityException e) {
								log.info("failed to force worker " + (i + 1)
										+ " to die");
							}
						}

						// workers[i] = null;
					}
				}

				log.info("All workers stopped successfully!");

			}

			try {
				if (p != null && p.getOutput() != null) {
					p.getOutput().flush();
					p.getOutput().close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			endtime = System.currentTimeMillis();
		}
	}

	/**
	 * initializes the data Generator generation process. Checks if all
	 * necessary preconditions are met.
	 * 
	 * @param p
	 *            The project
	 * @return true if start of generation process was successful
	 * @throws ConfigurationException
	 * @throws XmlException
	 */
	public synchronized void initialize(Project p)
			throws ConfigurationException, XmlException {

		if (successfullyInitialized) {
			successfullyInitialized = false;
		}

		if (p == null) {
			throw new ConfigurationException(
					"Project is null! Project configuraion xml was not loaded");
		}

		this.p = p;
		checkProjectConfigurationPreConds();

		// determine count of workers
		int workerCount = p.getWorkers();
		if (workerCount == Constants.INT_NOT_SET || workerCount < 1) {
			workerCount = Runtime.getRuntime().availableProcessors();
			p.setWorkers(workerCount); // causes p.initialize()
			log.info("Worker count was not specified manually. Detected "
					+ workerCount
					+ " available processors and start now same amount of workers");
		}

		// now init Project. Create per worker RNGs and let custom plugins do
		// their stuff!
		p.initialize(workerCount);

		// should not happen beyond this point!
		if (!p.isInitialized()) {
			throw new ConfigurationException(
					"Project is not initialized which means the number of workers was not set or invalid. Current number is: "
							+ p.getWorkers());
			// initialize Project. Obsoletet: initializes itself when
			// p.setWorkers is called, so this should never happen
			// p.initialize();
		}

		// init ouput
		p.getOutput().initialize(workerCount);

		// crate worker threads and init them
		workers = new Worker[workerCount];
		for (int threadID = 0; threadID < workers.length; threadID++) {
			workers[threadID] = new Worker(p, workerCount, (threadID + 1),
					p.getScheduler());
			workers[threadID].initialize();
		}

		// init Scheduler
		scheduler = p.getScheduler();
		scheduler.setDataGenerator(this);
		scheduler.initialize(workers);

		// init ok
		successfullyInitialized = true;
	}

	public synchronized boolean isInitialized() {
		return successfullyInitialized;
	}

	/**
	 * if true, DataGenerator calls Controller.execute(Command.quit) after all
	 * workers have finished;
	 * 
	 * @param b
	 */
	public synchronized void setCloseAfterFinish(boolean b) {
		closeAfterFinish = b;

	}

	public synchronized boolean getCloseAfterFinish() {

		return closeAfterFinish;
	}

	private void checkProjectConfigurationPreConds()
			throws ConfigurationException {
		if (p == null) {
			throw new ConfigurationException(
					"Should not happen: Project was null!");
		}

		// config file is loaded
		if (!p.projectConfigLoaded()) {
			throw new ConfigurationException(
					"Project cofiguration file was not loaded!");
		}

	}

	private void checkGenerators(Project p) {
		// check all generators
		long start = System.currentTimeMillis();
		Field[] fields;
		Table[] tables = p.getChilds();
		Table table;
		GenerationContext generationContext = new GenerationContext();
		generationContext.set(10, 1, 5, 2, p, 1, 1);
		AbstractPDGFRandom generatorTestRNG = p.getNewElementRng(p.getSeed());
		log.debug("Checking all Generators for Generator.getNextValue() -> FieldValue.getValue() != null");
		for (int tableId = 0; tableId < tables.length; tableId++) {
			table = tables[tableId];
			fields = table.getChilds();

			for (int fieldId = 0; fieldId < fields.length; fieldId++) {
				FieldValueDTO fv = fields[fieldId].getNewFieldValueDTO();
				fields[fieldId].getGenerator(0).getNextValue(generatorTestRNG,
						generationContext, fv);

				// seems that Generator was not implemented
				if (fv.getValue() == null) {

					// throw new RuntimeException(
					// "It seams that  Generator.getNextValue()  in class "
					// + fields[fieldId].getGenerator().getClass()
					// .getName()
					// +
					// " was not correctly implemented because getNextValue returnd Null in shared Object for FieldValue.getValue() !\n Debug info: this happend while proccessing "
					// + fields[fieldId].getGenerator()
					// .getNodeInfo());
				}
			}
		}
		log.debug("Checking all Generators: Done, Generators seem ok. Completed in ms: "
				+ (System.currentTimeMillis() - start));

	}

	private void generateReport() {
		long maxTime = 0;
		long averageTime = 0;
		long time;
		long overall;

		if (endtime == Constants.LONG_NOT_SET) {
			endtime = (System.currentTimeMillis());
		}
		overall = endtime - starttime;

		if (workers != null && workers[0] != null) {
			for (int i = 0; i < workers.length; i++) {
				time = workers[i].getTimeElapsedWorker();
				averageTime += time;
				maxTime = Math.max(maxTime, time);

			}

			averageTime = averageTime / workers.length;
			log.info("REPORT\n=======================\nMax time of a worker: "
					+ maxTime + "ms average time: " + averageTime
					+ "\nGenerator overall time: " + overall + "ms");

			// Generate statistics file

			String statisticsFileName = "GeneratorStatistics_1.csv";
			String outDir = "";
			String path = statisticsFileName;
			long size = 0;

			/*
			 * if output writes to disk it may provides information about how
			 * much bytes where written so far: calc Megabyte per second from
			 * this
			 */
			if (p.getOutput() instanceof FileOutput) {
				FileOutput out = ((FileOutput) p.getOutput());

				size = out.getWritenBytes();

				outDir = out.getOutputDir();
				if (outDir != null && !outDir.isEmpty()) {
					path = outDir
							+ (outDir.endsWith(File.separator) ? ""
									: File.separator) + statisticsFileName;
				} else {
					outDir = statisticsFileName;
				}

			}
			FileWriter fw = null;
			File f = new File(path);

			StringBuilder csvOut = new StringBuilder();

			// header only for first node

			csvOut.append("Node;Worker max ms;Worker average ms;Generator overall ms;output size bytes");
			csvOut.append('\n');
			csvOut.append(1);
			csvOut.append(';');
			csvOut.append(maxTime);
			csvOut.append(';');
			csvOut.append(averageTime);
			csvOut.append(';');
			csvOut.append(overall);
			csvOut.append(';');
			csvOut.append(size);
			csvOut.append('\n');

			boolean error = true;
			try {

				f.createNewFile();
				if (!f.canWrite()) {
					log.warn("No write persmission. Could not write statistics file "
							+ f.getAbsolutePath());

				} else {
					fw = new FileWriter(f);

					fw.write(csvOut.toString());
					String msg = "finished processing after " + overall / 1000
							+ "s. Saved processing statistics file "
							+ f.getName() + " to " + f.getAbsolutePath();
					log.info(msg);
					echoToObservers(msg);
					fw.close();
					fw = null;
					error = false;
				}
			} catch (Exception e) {
				log.error("Could not write statistics file "
						+ f.getAbsolutePath() + " cause: " + e.getMessage());

			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				fw = null;
			}

			// error has occured, could not write statstics to file , echo it
			if (error) {
				echoToObservers(f.getName() + "\n" + csvOut.toString());
			}

		}

	}

	/**
	 * Called by Scheduler after all workers are done
	 * 
	 * @param string
	 *            , boolean successful
	 */
	public synchronized void notifyFinished(String msg, boolean successful) {
		successfulFinished = successful;
		echoToObservers(msg);
		stop();

		if (closeAfterFinish) {
			// wait for all threads to die
			log.debug("Generator: execute quit cmd");

			if (c != null)
				c.doQuitCMD();
		}
	}

	public void echoToObservers(String msg) {
		setChanged();
		notifyObservers(msg);
	}

	public boolean isStarted() {
		return started;
	}

	private void notify(String string) {

	}

}
