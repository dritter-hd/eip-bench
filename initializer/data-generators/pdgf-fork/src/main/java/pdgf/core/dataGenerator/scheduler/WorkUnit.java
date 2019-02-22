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

import pdgf.core.dbSchema.Table;
import pdgf.util.Constants;

/**
 * shared reused data transfer object between scheduler and workers
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class WorkUnit {

	public int workerId;
	public int workerCount;
	public int tableID;
	public Table table;
	public long workUnitId;
	public long rowStart;
	public long rowStop;
	public long rowCountOfWorkunit;
	public boolean pause;
	public boolean finished;
	public long lastProccessingTime;

	public WorkUnit() {
		super();
		this.workerId = Constants.INT_NOT_SET;
		this.workerCount = Constants.INT_NOT_SET;
		this.tableID = Constants.INT_NOT_SET;
		this.table = null;
		this.workUnitId = Constants.INT_NOT_SET;
		this.rowStart = Constants.LONG_NOT_SET;
		this.rowStop = Constants.LONG_NOT_SET;
		this.rowCountOfWorkunit = Constants.LONG_NOT_SET;
		this.pause = false;
		this.finished = false;
	}

}
