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

import org.slf4j.LoggerFactory;
import pdgf.core.FieldValueDTO;
import pdgf.core.RowDataDTO;
import pdgf.core.dataGenerator.scheduler.Progress;
import pdgf.core.dataGenerator.scheduler.Scheduler;
import pdgf.core.dataGenerator.scheduler.WorkUnit;
import pdgf.core.dbSchema.Field;
import pdgf.core.dbSchema.Project;
import pdgf.core.dbSchema.Table;
import pdgf.plugin.Output;
import pdgf.util.Constants;

import java.io.IOException;

/**
 * The worker is responsible for generating its part of the Projects data. Is
 * responsible for correctly seeding the hirachical Random Number Generators,
 * creating the Generation context an delegating generated data beween
 * generators and Output
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public final class Worker extends Thread {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Worker.class);
	private Object waiterMutex = new Object();
	// Worker context info
	private StringBuilder progressInformation = new StringBuilder();
	private Project project;
	private DataGenerator dg;
	private Scheduler scheduler;
	private WorkUnit workUnit;
	private boolean running;

	private int workerCount;
	private int workerID;
	private Output output;

	// statistics info
	private long percentageDone = 0;
	private long timeElapsedWU = 0;
	private long timeElapsedWorker = 0;
	private long timeRemaining = 0;
	private String currentTableName = "";

	/*
	 * eager initialization and variable reusage to avoid initialization and
	 * "new Object()" overhead in inner loop's
	 */
	// inner loop variables
	private long currentRowID;
	private int currentFieldId;

	private long currentTablePartionRowCount;
	private long workUnitRows;
	private long workUnitRowStart;
	private long workUnitRowStop;

	private long workerStartTime;
	private long lastStatusOutputTime = 0;
	private long workUnitStartTime = 0;

	private Table currentTable = null;
	private RowDataDTO rowData;
	private Field[] currentFields;

	private FieldValueDTO[] fieldValues;

	GenerationContext generationContext = new GenerationContext();
	private Table lastTable = null;
	private int nodeID;
	private boolean logStatistics = true;
	private boolean finished = false;
	private long rowsPerSecWu;

	/**
	 * Create new Worker
	 * 
	 * @param p
	 *            the Project this worker belongs to
	 * @param workerCount
	 *            number of threads.
	 * @param workerID
	 *            of this thread between [1, threadCount]
	 */
	public Worker(Project p, int workerCount, int workerID, Scheduler scheduler) {
		this.project = p;
		this.workerCount = workerCount;
		this.workerID = workerID;
		this.scheduler = scheduler;

	}

	public void initialize() {
		this.output = project.getOutput();
		this.nodeID = 1;
		this.rowData = new RowDataDTO();
		this.workUnit = new WorkUnit();
		workUnit.workerCount = workerCount;
		workUnit.workerId = workerID; // workerid starts at 1
		generationContext = new GenerationContext(workerID, workerCount,
				project);
		finished = false;

		// // static rngs: project, generatorTest, and row Rng
		// try {
		// localProjectRNG = RandomFactory.instance().getRNGClass(
		// project.getRngName());
		// localProjectRNG.setSeed(project.getSeed());
		//
		// localRowRNG = RandomFactory.instance().getRNGClass(
		// project.getRngName());
		//
		// } catch (ClassNotFoundException e1) {
		// // should not happen, because Rng classname was already checked
		// // at config init
		// e1.printStackTrace();
		// }

	}

	@Override
	public void run() {
		running = true;
		log.debug("Node: " + nodeID + " worker " + (workerID) + "/"
				+ workerCount + " is now running");
		workerStartTime = System.currentTimeMillis();
		workUnitStartTime = System.currentTimeMillis();
		while (running) {
			doWork();
		}

	}

	private void doWork() {
		timeElapsedWU = (System.currentTimeMillis() - workUnitStartTime);
		workUnit.lastProccessingTime = timeElapsedWU;
		scheduler.getNextWorkunit(workUnit);

		if (workUnit.pause) {

			try {
				output.flush(workerID);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (workUnit.finished) {
				updateStatistics();
				finished = workUnit.finished;
				log.debug("Node: " + nodeID + "worker " + (workerID) + "/"
						+ workerCount + " Status: finished generation after: "
						+ timeElapsedWU + "ms ");
			} else {

				log.info("Node: " + nodeID + "worker " + (workerID) + "/"
						+ workerCount + " Status: paused workunit: "
						+ timeElapsedWU + "ms ");
			}
			synchronized (waiterMutex) {
				try {
					waiterMutex.wait();
				} catch (InterruptedException e) {
					log.debug("Node: " + nodeID + "Worker " + (workerID)
							+ " was waiting: interupped");
				}

				if (!running) {
					// TODO! save current status;
					return;
				}
			}
		} else {

			doWorkUnit();
			updateStatistics();
		}
	}

	private void doWorkUnit() {
		workUnitStartTime = System.currentTimeMillis();

		// local caching of workunit information
		currentTable = workUnit.table;
		currentTableName = currentTable.getName();
		currentFields = currentTable.getChilds();
		workUnitRowStart = workUnit.rowStart;
		workUnitRowStop = workUnit.rowStop;
		currentTablePartionRowCount = workUnit.rowCountOfWorkunit;

		// Reinitialize shared objects for new partition
		if (lastTable == null || currentTable != lastTable) {
			rowData.reset(currentTable);
			fieldValues = rowData.getFieldValuesList();
		} else {
			for (FieldValueDTO fvdto : fieldValues) {
				fvdto.setValue(null);
				fvdto.setPlainValue(null);
			}

		}

		workUnitRows = workUnit.rowStop - workUnit.rowStart + 1;
		generationContext.set(currentTablePartionRowCount, workUnit.rowStart,
				workUnit.rowStop);

		log.debug("Node: " + nodeID + "worker " + (workerID) + "/"
				+ workUnit.workerCount + " is now proccessing table: \""
				+ currentTable.getName() + "\"");

		// iterate over rows in Table Partition of this WorkerThread
		generateRows();

		// one Table Finished, print info
		if (running) {
			log.debug("Node: " + nodeID + "worker " + (workerID) + "/"
					+ workerCount
					+ " finished proccessing a workunit for table: \""
					+ currentTable.getName() + "\" with " + workUnitRows
					+ "rows in: "
					+ (System.currentTimeMillis() - workUnitStartTime) + "ms");
		}
	}

	/**
	 * iterate over rows in Table Partition of this WorkerThread
	 */
	private void generateRows() {
		// iterate over rows in Table Partition of this WorkerThread
		for (currentRowID = workUnitRowStart; currentRowID <= workUnitRowStop; currentRowID++) {
			if (!running) {
				// TODO! save current status;
				return; // break;
			}

			generateRow();

			updateWuProgressInfo();
		}// one Table Finished

	}

	private void generateRow() {
		// reseed Row Rng
		// localRowRNG.setSeed(localTableRNG.nextLong());

		// update row in shared objects
		generationContext.setCurrentRow(currentRowID);
		rowData.setRow(currentRowID);

		// most inner loop: iterate over fields in row
		for (currentFieldId = 0; currentFieldId < currentFields.length; currentFieldId++) {
			currentFields[currentFieldId].getFieldValueForRow(
					generationContext, fieldValues[currentFieldId]);
		}

		// one Row finished, write row to output
		try {
			output.write(rowData, workerID);
		} catch (IOException e) {
			// FIXME! System.exit(-1);
			running = false;
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void updateWuProgressInfo() {
		// update Progress information
		if (logStatistics) {
			if ((System.currentTimeMillis() - lastStatusOutputTime) > Constants.GENERATION_PROGRESS_UPDATE_INTERVALL) {
				updateStatistics();
				lastStatusOutputTime = System.currentTimeMillis();
				progressInformation.setLength(0); // reset
				progressInformation.append("Node: ");
				progressInformation.append(nodeID);
				progressInformation.append(" ");
				progressInformation.append((workerID));
				progressInformation.append("/");
				progressInformation.append(workerCount);
				progressInformation.append(" \"");
				progressInformation.append(currentTableName);
				progressInformation.append("\" Status cur. workunit: ");
				progressInformation.append(getPercentageDone());
				progressInformation.append("% remaining: ");
				progressInformation.append((getTimeRemaining() / 1000));
				progressInformation.append("s elapsed: ");
				progressInformation.append((getTimeElapsedWU() / 1000));
				progressInformation.append("s current row: ");
				progressInformation.append(currentRowID);
				progressInformation.append(" Overall time:");
				progressInformation.append((getTimeElapsedWorker() / 1000));
				progressInformation.append("s");
				log.info(progressInformation.toString());
			}
		}
	}

	/**
	 * @return the percentageDone for the current table
	 */
	public long getPercentageDone() {

		return percentageDone;
	}

	/**
	 * @return the timeElapsed for the current workunit
	 */
	public long getTimeElapsedWU() {

		return timeElapsedWU;
	}

	/**
	 * @return the timeElapsed for the current table
	 */
	public long getTimeElapsedWorker() {

		return timeElapsedWorker;
	}

	public void updateStatistics(Progress p) {

		updateStatistics();
		p.set(rowsPerSecWu, workUnitRowStart, workUnitRowStop,
				timeElapsedWorker, timeElapsedWU, timeRemaining,
				percentageDone, currentTable);

	}

	/**
	 * Updates processing statistics like timeElapsedWU,
	 * timeElapsedWorker,percentageDone,timeRemaining,rowsPerSecWu If values
	 * &lt; 1 values are set to 1 to avoid divide by zero
	 */
	public void updateStatistics() {
		// if still running update value
		if (running && !finished) {
			timeElapsedWU = (System.currentTimeMillis() - workUnitStartTime);
			timeElapsedWU = timeElapsedWU == 0 ? 1 : timeElapsedWU;
			timeElapsedWorker = (System.currentTimeMillis() - workerStartTime);
			timeElapsedWorker = timeElapsedWorker == 0 ? 1 : timeElapsedWorker;

			percentageDone = ((100 * (currentRowID - workUnitRowStart + 1)) / (workUnitRows + 1));
			percentageDone = percentageDone == 0 ? 1 : percentageDone;

			timeRemaining = (timeElapsedWU * (100 - percentageDone) / percentageDone);
			timeRemaining = timeRemaining == 0 ? 1 : timeRemaining;

			rowsPerSecWu = (currentRowID - workUnitRowStart) / timeElapsedWU
					* 1000;
			rowsPerSecWu = rowsPerSecWu == 0 ? 1 : rowsPerSecWu;
		}

	}

	/**
	 * specifies if Worker should do his own statistics logging
	 * 
	 * @param doLogging
	 */
	public void logStatistics(boolean doLogging) {
		logStatistics = doLogging;
	}

	/**
	 * @return the timeRemaining for the current table
	 */
	public long getTimeRemaining() {

		return timeRemaining;
	}

	/**
	 * @return the currentTable
	 */
	public String getCurrentTable() {
		return currentTableName;
	}

	public void stopWorker() {
		running = false;
		if (!finished) {
			updateStatistics();
		}

		synchronized (waiterMutex) {
			waiterMutex.notify();
		}

	}

	public void continueWorker() {
		if (running) {
			finished = false;
			synchronized (waiterMutex) {
				waiterMutex.notify();
			}
		}

	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public int getWorkerID() {
		return workerID;
	}

	public boolean isRunning() {
		return running;
	}

}
