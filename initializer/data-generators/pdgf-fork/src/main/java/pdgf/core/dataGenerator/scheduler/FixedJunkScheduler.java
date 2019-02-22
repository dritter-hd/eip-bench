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
package pdgf.core.dataGenerator.scheduler;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import pdgf.core.Element;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.DataGenerator;
import pdgf.core.dataGenerator.Worker;
import pdgf.core.dbSchema.Project;
import pdgf.core.dbSchema.Table;
import pdgf.core.exceptions.XmlException;
import pdgf.output.FileOutput;
import pdgf.plugin.AbstractScheduler;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;

/**
 * Does partitioning of workload by statically dividing it by the count of
 * workers. If one thread may be slower than other threads, not shifting of
 * workload is done.
 * 
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class FixedJunkScheduler extends AbstractScheduler {
	private static final Logger log = LoggerFactory.getLogger(FixedJunkScheduler.class);
	private static final String NODE_PARSER_DisableAutoStatistics = "disableAutomaticStatistics";

	public FixedJunkScheduler() throws XmlException {
		super(
				"This Scheduler does not require any disk read I/O or Network I/O. The workload is devided evenly among participating nodes. No load balancing is done between nodes.");
		// TODO Auto-generated constructor stub
	}

	private static boolean disableAutostatistics = false;
	private long starttime = -1;
	private Project p;
	private int lastTableID;
	private long lastWorkerChunkId[];
	private int lastTableIdWorker[];
	private long nodeTableStart[];
	private long nodeTableStop[];
	private int workerCount;
	private long[] nodeTablePartSize;
	private Table[] tables;
	private Worker[] workers;
	private int finishedWorkers;
	private Object finishedMutex = new Object();
	private AtomicLong uniqueChunkId = new AtomicLong(0);
	private ProgressStatistics<FixedJunkScheduler> statistics;

	@Override
	public synchronized void getNextWorkunit(WorkUnit wu) {

		// workerid's from [1,n] but array starts at 0, so in per worker arrays
		// we always do wu.workerId-1

		/*
		 * How this method works:
		 * 
		 * The spliting of the workload is precalculated for all workers in the
		 * initialize() method and cached in per thread/worker arrays. this way
		 * we do not need to synchronize annything here.
		 * 
		 * as we statically divide the workload between workers, a worker only
		 * calls this method after he finished all his work for one table, so we
		 * select the next table for him
		 */

		// select next table, skip table if table is excluded.
		do {
			/*
			 * worker asked for next wu but has allready recived his last work
			 * unit. OR all remaining tables where exclued from generation
			 * 
			 * so tell him there is nothing more to do
			 */
			if (lastTableIdWorker[wu.workerId - 1] == lastTableID) {
				setFinished(wu);
				return;
			}
			lastTableIdWorker[wu.workerId - 1]++;
			wu.tableID = lastTableIdWorker[wu.workerId - 1]; // set next table
			wu.table = tables[wu.tableID]; // set next table
			// System.out.println("nextTable is for worker: " + wu.workerId
			// +" is " + wu.table.getName() + " exluded?:"+wu.table.isExcluded()
			// );
		} while (wu.table.isExcluded());

		// id

		/*
		 * starting row of this nodes partition was precalculated. Now we devide
		 * the node partition between the workers. We calculate the starting
		 * point for this worker by adding the staring offset for him to the
		 * starting offset (row) of this nodes partition
		 */
		wu.rowStart = nodeTableStart[wu.tableID]
				- 1
				+ StaticHelper.getPartitionStart(nodeTablePartSize[wu.tableID],
						workerCount, wu.workerId);

		// do the same for stop.
		wu.rowStop = nodeTableStart[wu.tableID]
				- 1
				+ StaticHelper.getPartitionStop(nodeTablePartSize[wu.tableID],
						workerCount, wu.workerId);
		wu.rowCountOfWorkunit = wu.rowStop - wu.rowStart + 1;

		// give each junk a unique id, currently not used by workers or
		// output, but may be handy for later internal use.
		lastWorkerChunkId[wu.workerId - 1] = uniqueChunkId.incrementAndGet();
		wu.workUnitId = lastWorkerChunkId[wu.workerId - 1];

	}

	public void start() {

		starttime = System.currentTimeMillis();
		statistics = new ProgressStatistics<FixedJunkScheduler>(workers, this);

		// start automatic statistics output thread
		if (!disableAutostatistics) {
			statistics.start();
		}

	}

	public void stop() {
		if (statistics != null) {
			statistics.stopMe();

			try {
				statistics.interrupt();
				statistics.join(2000);
			} catch (InterruptedException e1) {

			}
		}
	}

	private void setFinished(WorkUnit wu) {
		wu.pause = true; // finished
		wu.finished = true;

		// sychronize this as finishedWorkers++; is not atomic!
		synchronized (finishedMutex) {
			/*
			 * as a finished worker does not ask for a next workunit this is
			 * correct, otherwise if you plan to re-awake him by calling
			 * continueWorker() you would need to store and check the finished
			 * status of all with a different way
			 */

			finishedWorkers++;

			// all workers finished, tell data generator about it. Dg then
			// will stop & fulsh all workers & output, generate a report and
			// kill the threads
			if (finishedWorkers == workerCount) {
				getDataGenerator().notifyFinished("", true);
			}
		}
	}

	@Override
	public Progress getProgress() {

		return statistics.getProgress();
	}

	@Override
	protected void configParsers() {
		addNodeParser(new DisableAutoStatisticsNodeParser(false, true, this));

	}

	@Override
	public void initialize(Worker[] workers) {
		this.p = this.getParent();
		this.workers = workers;
		this.workerCount = workers.length;
		this.tables = parent.getChilds();
		this.lastTableID = tables.length - 1;
		this.finishedWorkers = 0;
		nodeTableStart = new long[tables.length];
		nodeTableStop = new long[tables.length];
		nodeTablePartSize = new long[tables.length];
		lastWorkerChunkId = new long[workerCount];
		lastTableIdWorker = new int[workerCount];

		long rows;

		for (int i = 0; i < tables.length; i++) {

			// ALLWAYS! use scaledSize! this method does take scaling already
			// into account
			rows = tables[i].getScaledTableSize();

			// is the same on every node?

			nodeTableStart[i] = 1;
			nodeTableStop[i] = rows;
			nodeTablePartSize[i] = rows;

		}

		for (int i = 0; i < workerCount; i++) {
			lastTableIdWorker[i] = -1;

		}

	}

	private class DisableAutoStatisticsNodeParser extends Parser<Element> {

		public DisableAutoStatisticsNodeParser(boolean isRequired,
				boolean used, Element e) {
			super(
					isRequired,
					used,
					NODE_PARSER_DisableAutoStatistics,
					e,
					"If true: disables automatic update and output of Statistics from this Scheduler by a seperate thread. Values: {true | false} Default: "
							+ disableAutostatistics);
		}

		@Override
		protected void parse(Node node) throws XmlException {
			disableAutostatistics = Boolean.parseBoolean(node.getTextContent());
			log.debug("FixedJunkScheduler - Automatic statistics update enabled: "
					+ !disableAutostatistics);
		}
	}

	private class ProgressStatistics<S extends Scheduler> extends Thread {

		private boolean running = true;
		private S parent = null;
		private long updateIntervall = Constants.GENERATION_PROGRESS_UPDATE_INTERVALL;
		private Worker[] workers;
		private StringBuilder progressInformation = new StringBuilder();
		private String lastProgresString = "";
		private Object progressMutex = new Object();
		private FileOutput out = null;
		private Progress prog = new Progress();
		private DataGenerator dataGenerator;

		public void stopMe() {
			running = false;
		}

		/**
		 * Standard out for statistics
		 * 
		 * @param out
		 */
		public ProgressStatistics(Worker[] workers, S parent) {

			this.workers = workers;
			this.parent = parent;
			for (int i = 0; i < workers.length; i++) {

				workers[i].logStatistics(false);
			}
			if (p.getOutput() instanceof FileOutput) {
				out = (FileOutput) p.getOutput();
			}
			dataGenerator = this.parent.getDataGenerator();
		}

		public void run() {
			while (running) {
				try {

					sleep(updateIntervall);
				} catch (InterruptedException e) {

				}
				if (running) {
					updateProgress();
					dataGenerator.echoToObservers(lastProgresString);
				}
			}
		}

		public String getLastProgresMSG() {

			return lastProgresString;

		}

		public Progress getLastProgress() {
			return prog;
		}

		public Progress getProgress() {

			return updateProgress();

		}

		private synchronized Progress updateProgress() {
			Progress workerProgress = new Progress();

			long timeRemaining;
			long timeEleapsed;
			HashMap<Table, Integer> currentTables = new HashMap<Table, Integer>();
			HashMap<Table, Long> currentTablesRemainingTimes = new HashMap<Table, Long>();
			HashMap<Table, Long> currentTablesPercentages = new HashMap<Table, Long>();

			Integer count; // count: how much workers process which table
			Long time;
			Long averagePercentage = new Long(0);
			Table curTable = null;
			long rowsPerSec = 0;
			synchronized (progressMutex) {

				for (int i = 0; i < workers.length; i++) {

					// check if a worker has died unexpected
					if (!workers[i].isAlive()) {
						boolean allDead = true;
						for (int j = 0; j < workers.length; j++) {
							if (workers[i].isAlive()) {
								allDead = false;
							}
						}
						if (allDead) {
							getDataGenerator().notifyFinished(
									"All Workers died unexpeced!", false);
							return null;
						}
					}

					workers[i].updateStatistics(workerProgress);// get fresh
					// values

					curTable = workerProgress.getCurrentTable();
					rowsPerSec += workerProgress.getRowsPerSec();
					// count: how much workers process which table
					count = currentTables.get(curTable);
					time = currentTablesRemainingTimes.get(curTable);
					averagePercentage = currentTablesPercentages.get(curTable);

					// if cur table not yet in map, put it there
					if (count == null) {
						currentTables.put(curTable, 1);
						currentTablesRemainingTimes.put(curTable,
								workerProgress.getTimeRemaining());
						currentTablesPercentages.put(curTable,
								workerProgress.getPercentageDone());

						/*
						 * cur table is processed by other worker to, increase
						 * count and calc average time & percentage
						 */

					} else {
						count = count + 1;
						currentTables.put(curTable, count);
						currentTablesRemainingTimes.put(
								curTable,
								Math.max(time,
										workerProgress.getTimeRemaining()));
						averagePercentage = averagePercentage
								+ workerProgress.getPercentageDone();
						currentTablesPercentages.put(curTable,
								averagePercentage);

						// count +=1;
						// time = Math.max(time, prog.getTimeRemaining());
						// averagePercentage += prog.getPercentageDone();

					}

				}

				rowsPerSec = rowsPerSec / workers.length;

				Set<Entry<Table, Integer>> entrys = currentTables.entrySet();
				int max = 0;
				Table winningTable = null;

				// select the table most workers are currently processing, only
				// display winning table
				for (Entry<Table, Integer> entry : entrys) {
					if (entry.getValue() > max) {
						max = entry.getValue();
						winningTable = entry.getKey();
					}
				}

				timeRemaining = currentTablesRemainingTimes.get(winningTable);
				averagePercentage = currentTablesPercentages.get(winningTable)
						/ max; // over winning
				// table

				long size = -1;

				//

				progressInformation.setLength(0); // reset
				progressInformation.append("\"");
				progressInformation.append(winningTable.getName());

				progressInformation.append("\" ");
				progressInformation.append(winningTable.getElementID() + 1);
				progressInformation.append("/");
				progressInformation.append(p.getChildsCount());
				progressInformation.append(" | ");
				progressInformation.append(averagePercentage);
				progressInformation.append("% remaining: ");
				progressInformation.append(timeRemaining / 1000);
				progressInformation.append("s elapsed: ");
				progressInformation
						.append((System.currentTimeMillis() - starttime) / 1000);
				progressInformation.append("s");

				/*
				 * if output writes to disk it may provides information about
				 * how much bytes where written so far: calc Megabyte per second
				 * from this
				 */
				if (out != null) {
					size = out.getWritenBytes();
					if (size > -1) {
						progressInformation.append(" | ");
						progressInformation.append(size * 1000 / 1024 / 1024
								/ ((System.currentTimeMillis() - starttime)));
						progressInformation.append("MiB/s");
					}

				}

				lastProgresString = progressInformation.toString();
				prog.set(rowsPerSec,
						nodeTableStart[winningTable.getElementID()],
						nodeTableStop[winningTable.getElementID()],
						((System.currentTimeMillis() - starttime)), -1,
						timeRemaining, averagePercentage, winningTable);

			}
			return prog;
		}

	}

}
