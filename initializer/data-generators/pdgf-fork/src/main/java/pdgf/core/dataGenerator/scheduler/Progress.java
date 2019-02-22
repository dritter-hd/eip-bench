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

/**
 * currently not used
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class Progress {
	private long rowsPerSec;
	private long rowStart;
	private long rowStop;
	private long runningTime;
	private long runningTimeWorkunit;
	private long timeRemaining;
	private long percentageDone;
	private Table currentTable;

	public Progress() {
	};

	public Progress(long rowsPerSec, long rowStart, long rowStop,
			long runningTime, long runningTimeWorkunit, long percentageDone,
			long timeRemaining, Table currentTable) {
		set(rowsPerSec, rowStart, rowStop, runningTime, runningTimeWorkunit,
				percentageDone, timeRemaining, currentTable);
	}

	public void set(long rowsPerSec, long rowStart, long rowStop,
			long runningTime, long runningTimeWorkunit, long timeRemaining,
			long percentageDone, Table currentTable2) {

		this.rowsPerSec = rowsPerSec;
		this.rowStart = rowStart;
		this.rowStop = rowStop;
		this.runningTime = runningTime;
		this.percentageDone = percentageDone;
		this.currentTable = currentTable2;
		this.runningTimeWorkunit = runningTimeWorkunit;
		this.setTimeRemaining(timeRemaining);
	}

	public long getRowsPerSec() {
		return rowsPerSec;
	}

	public void setRowsPerSec(long rowsPerSec) {
		this.rowsPerSec = rowsPerSec;
	}

	public long getRowStart() {
		return rowStart;
	}

	public void setRowStart(long rowStart) {
		this.rowStart = rowStart;
	}

	public long getRowStop() {
		return rowStop;
	}

	public void setRowStop(long rowStop) {
		this.rowStop = rowStop;
	}

	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	public long getPercentageDone() {
		return percentageDone;
	}

	public void setPercentageDone(long percentageDone) {
		this.percentageDone = percentageDone;
	}

	public Table getCurrentTable() {
		return currentTable;
	}

	public void setCurrentTable(Table currentTable) {
		this.currentTable = currentTable;
	}

	public void setRunningTimeWorkunit(long runningTimeWorkunit) {
		this.runningTimeWorkunit = runningTimeWorkunit;
	}

	public long getRunningTimeWorkunit() {
		return runningTimeWorkunit;
	}

	public void setTimeRemaining(long timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public long getTimeRemaining() {
		return timeRemaining;
	}

}
