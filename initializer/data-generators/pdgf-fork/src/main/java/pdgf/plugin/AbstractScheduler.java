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
package pdgf.plugin;

import pdgf.core.Element;
import pdgf.core.dataGenerator.DataGenerator;
import pdgf.core.dataGenerator.Worker;
import pdgf.core.dataGenerator.scheduler.Progress;
import pdgf.core.dataGenerator.scheduler.Scheduler;
import pdgf.core.dataGenerator.scheduler.WorkUnit;
import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.XmlException;

/**
 * Superclass of all Schedulers
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
abstract public class AbstractScheduler extends Element<Element, Project>
		implements Scheduler {

	private DataGenerator dataGenerator;

	public DataGenerator getDataGenerator() {
		return dataGenerator;
	}

	public AbstractScheduler(String description) throws XmlException {
		super("scheduler", description);

	}

	public void setDataGenerator(DataGenerator dataGenerator) {
		this.dataGenerator = dataGenerator;
	}

	/**
	 * Notifies the Datagenerator that all workers finished processing their
	 * last workunit.
	 */
	public void notifyDgFinished(String msg, boolean successful) {
		getDataGenerator().notifyFinished(msg, successful);
	}

	public void getNextWorkunit(WorkUnit wu) {
		// TODO Auto-generated method stub

	}

	public Progress getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initialize(Worker[] workers) {
		// TODO Auto-generated method stub

	}

}
