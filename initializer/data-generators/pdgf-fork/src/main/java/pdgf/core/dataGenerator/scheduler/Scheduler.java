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

import pdgf.core.dataGenerator.DataGenerator;
import pdgf.core.dataGenerator.Worker;
import pdgf.core.dbSchema.Table;
import pdgf.util.Constants;

/**
 * The scheduler is responsible to divide the workload even between all
 * participating physical nodes and the workerthread per node. Before you try to
 * write your own Scheduler read and understand the implementation of
 * FixedJunkScheduler who does a simple static precalculated division of
 * workload between nodes and workerthreads. <br/>
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public interface Scheduler {

	/**
	 * Important notice:<br/>
	 * This method is called by the worker threads asynchronously. Differing
	 * from all other plugins like generator you have to worry about concurency
	 * here, as the scheduler is one of the core components.<br/>
	 * <br/>
	 * The scheduler is responsible to divide the workload even between all
	 * participating physical nodes and the workerthread per node. Before you
	 * try to write your own Scheduler read and understand the implementation of
	 * FixedJunkScheduler who does a simple static precalculated division of
	 * workload between nodes and workerthreads. <br/>
	 * <br/>
	 * 
	 * The workers deliver their WorkUnit object (this is a shared reusable
	 * object between a worker and this scheduler) to this method.<br/>
	 * This method is responsible to tell the worker which rows of which table
	 * he has to calculate next by putting the required information into the
	 * workunit obj which contents will later be used by the worker.<br/>
	 * <br/>
	 * <br/>
	 * 
	 * A short list of what you have to do:
	 * <ul>
	 * <li>partition the workload between the participating nodes.Required
	 * information in:<br/>
	 * <ul>
	 * <li>parent.getNodeCount()</li>
	 * <li>parent.getNodeNumber()</li>
	 * </ul>
	 * </li>
	 * <li>partition the workload of one node between its workers. In
	 * initialize() you get a reference to all participating workers</li>
	 * <li>tell the workers if they are finished or have to wait by setting the
	 * according flags in their workunit object
	 * <ul>
	 * <li>wu.pause = true;</li>
	 * <li>wu.finished = true;</li>
	 * </ul>
	 * <li>respect the flags that are set for an table! *
	 * <ul>
	 * <li> {@link Table#isExcluded()} do not generate values for this table.</li>
	 * <li> {@linkplain Table#isStatic()} this table must be generated completely
	 * on each node! Note: Exclusion dominates over static</li>
	 * <li> {@linkplain Table#isFixedSize()} table does not scale (you already
	 * get the correct final table size if you request it with
	 * {@linkplain Table#getScaledTableSize()) }</li>
	 * </ul>
	 * </li>
	 * 
	 * To continue a worker you have to call continueWorker() on him (for this
	 * you may want to safe a local reference to the Worker[] in initialize()) ,
	 * waking him up. After this the worker will requests the nextWorkunit from
	 * this scheduler. Don't miss to clear the running or pause flags. You are
	 * the scheduler, so the worker wont do that for you!</li>
	 * <li>if all workers are finished you have to tell the DataGenerator class
	 * about it (as only the scheduler knows if everything is done)! by calling
	 * this.notifyDgFinished() or this.getDataGenerator().notifyFinished();</li>
	 * </ul>
	 * 
	 * Be very careful with synchronizing here! as it has a huge performance
	 * impact. You may consider to do a per thread/worker caching/handling of
	 * things or do some precalculation in the initialize(..) method (and
	 * nowhere else because of the initializing structure and only at this point
	 * the final number of wokers will be known as it is provided as method
	 * arguments). For this each workunit wu contains the unique ID of his
	 * worker which is equal to worker.getWorkerID() (workerid starting at 1) <br/>
	 * 
	 * 
	 * @param wu
	 *            the WorkunitObject belonging to the calling worker
	 * 
	 */
	public void getNextWorkunit(WorkUnit wu);

	/**
	 * This method is considered for use in a GUI to provide the gui with a low
	 * level formated progress details. Every scheduler is responsible for
	 * providing information about the current progress of the generation. He
	 * has to inform the user about this. As only the Scheduler knows how much
	 * of the overall workload has been done only he can provide the user with
	 * this kind of information. You may use
	 * {@linkplain DataGenerator#echoToObservers(String)} for this to display
	 * e.g every 10 seconds the current progress to the user.
	 * 
	 * @see Constants#GENERATION_PROGRESS_UPDATE_INTERVALL
	 */
	public Progress getProgress();

	/**
	 * Starts scheduler. This method is called by Data Generator after
	 * initialize() and short before worker[i].start Worker[i] may then call
	 * getNextWorkUnit
	 */
	public void start();

	/**
	 * Stops this scheduler. Called by Data Generator if DataGenerator.stop() is
	 * executed.
	 */
	public void stop();

	/**
	 * Use this and only this method to do precalculation/initializing stuff! <br/>
	 * Worker w = workers[i] where i equals w.getWorkerID()-1
	 * 
	 * @param workers
	 */
	public void initialize(Worker[] workers);

	/**
	 * parent datagenerator of this scheduler
	 * 
	 * @return
	 */
	public DataGenerator getDataGenerator();

	/**
	 * * parent datagenerator of this scheduler
	 * 
	 * @param dataGenerator
	 */
	public void setDataGenerator(DataGenerator dataGenerator);

	/**
	 * Notifies the Datagenerator that all workers finished processing their
	 * last workunit.
	 */
	public void notifyDgFinished(String msg, boolean successful);
}
