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

import pdgf.core.dbSchema.Project;
import pdgf.core.dbSchema.Table;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;

/**
 * Holds the current Context of a threads (workers)generation proccess
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class GenerationContext implements Cloneable {
	private long rows = Constants.LONG_NOT_SET;
	private long rowStart = Constants.LONG_NOT_SET;
	private long rowStop = Constants.LONG_NOT_SET;
	private long currentRow = Constants.LONG_NOT_SET;

	private int workerId = Constants.INT_NOT_SET;
	private int workerCount = Constants.INT_NOT_SET;
	private Project project = null;

	public GenerationContext() {
	}

	public GenerationContext(int workerId, int workerCount, Project p) {
		this.workerCount = workerCount;
		this.workerId = workerId;
		this.project = p;
	}

	public GenerationContext(GenerationContext context) {
		set(context.rows, context.rowStart, context.rowStop,
				context.currentRow, context.project, context.workerId,
				context.workerCount);
	}

	/**
	 * Constructor for Referenced/Foreign Fields
	 * 
	 * @param context
	 * @param row
	 * @param rowStart
	 * @param rowStop
	 */
	public GenerationContext(GenerationContext context, long row,
			long rowStart, long rowStop) {
		set(row, context.rows, rowStart, rowStop, context.project,
				context.workerId, context.workerCount);
	}

	/**
	 * safes given parameters in this generation contex. See coresponding single
	 * getters and setters for a detailed description on the parameters.
	 * 
	 * @param rows
	 * @param rowStart
	 * @param rowStop
	 */
	public void set(long rows, long rowStart, long rowStop) {
		this.rows = rows;
		this.rowStart = rowStart;
		this.rowStop = rowStop;
		this.currentRow = -1;

	}

	/**
	 * safes given parameters in this generation contex. See coresponding single
	 * getters and setters for a detailed description on the parameters.
	 * 
	 * @param currentRow
	 * @param rows
	 * @param rowStart
	 * @param rowStop
	 * @param project
	 * @param threadID
	 * @param threadCount
	 */
	public void set(long currentRow, long rows, long rowStart, long rowStop,
			Project project, int threadID, int threadCount) {
		this.rows = rows;
		this.rowStart = rowStart;
		this.rowStop = rowStop;
		this.currentRow = currentRow;
		this.project = project;
		this.workerId = threadID;
		this.workerCount = threadCount;
	}

	public void set(long currentRow, long rowsTotal, long rowStart,
			long rowStop, Project project) {
		this.rows = rowsTotal;
		this.rowStart = rowStart;
		this.rowStop = rowStop;
		this.currentRow = currentRow;
		this.project = project;

	}

	/**
	 * safes given parameters in this generation contex. See coresponding single
	 * getters and setters for a detailed description on the parameters.
	 * 
	 * @param threadID
	 * @param threadCount
	 * @param currentRow
	 * @param t
	 */
	public void set(int threadID, int threadCount, long currentRow, Table t) {
		this.currentRow = currentRow;
		this.project = t.getParent();
		this.rows = t.getScaledTableSize();

		this.rowStart = 1 + StaticHelper.getPartitionStart(rows, threadCount,
				threadID);
		this.rowStop = 1 + StaticHelper.getPartitionStop(rows, threadCount,
				threadID);
	}

	/**
	 * number of rows in this Partition (equals getRowStop()-getRowStart() )
	 * 
	 * @return the rows
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * number of rows in this Partition (equals getRowStop()-getRowStart() )
	 * 
	 * @param rows
	 *            the rows to set
	 */
	public void setRows(long rows) {
		this.rows = rows;
	}

	/**
	 * starting row of this generation contexts partition Rows,(min 1)
	 * 
	 * @return the rowStart
	 */
	public long getRowStart() {
		return rowStart;
	}

	/**
	 * starting row of this generation contexts partition (min 1)
	 * 
	 * @param rowStart
	 *            the rowStart to set
	 */
	public void setRowStart(long rowStart) {
		this.rowStart = rowStart;
	}

/**
	 * last row of this generation contexts partition. min 1, max {@link Table#getScaledTableSize()
	 * @return the rowStop
	 */
	public long getRowStop() {
		return rowStop;
	}

/**
	 * last row of this generation contexts partition. min 1, max {@link Table#getScaledTableSize()
	 * @param rowStop
	 *            the rowStop to set
	 */
	public void setRowStop(long rowStop) {
		this.rowStop = rowStop;
	}

	/**
	 * the row currently processed min 1
	 * 
	 * @return the currentRow
	 */
	public long getCurrentRow() {
		return currentRow;
	}
	
	
	/**
	 * equals getCurrentRow()
	 * 
	 * @return the currentRow
	 */
	public long getID() {
		return currentRow;
	}

	/**
	 * the row currently processed min 1
	 * 
	 * @param currentRow
	 *            the currentRow to set
	 */
	public void setCurrentRow(long currentRow) {
		this.currentRow = currentRow;
	}
	
	/**
	 * equals setCurrentRow()
	 * 
	 * @param currentRow
	 *            the currentRow to set
	 */
	public void setID(long currentRow) {
		this.currentRow = currentRow;
	}


	/**
	 * the parent project
	 * 
	 * @return
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Worker ID starts at 1 [1, n] n= getWorkerCount() <br/>
	 * if not specified value is Constants.INT_NOT_SET
	 * 
	 * @return
	 */
	public int getWorkerID() {
		return workerId;
	}

	/**
	 * Number of workers. Worker ID starts at 1 [1, n] n = getWorkerCount() <br/>
	 * if not specified value is Constants.INT_NOT_SET
	 * 
	 * @return
	 */
	public int getWorkerCount() {
		return workerCount;
	}

}
