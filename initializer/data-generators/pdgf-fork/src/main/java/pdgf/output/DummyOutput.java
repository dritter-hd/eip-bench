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
package pdgf.output;

import java.io.IOException;

import pdgf.core.RowDataDTO;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Output;

/**
 * its a dummy, does nothing
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public class DummyOutput extends Output {

	public DummyOutput() throws XmlException {
		super(
				"Does nothing. Data is not safed or cached in any way. All methods are empty. Return values are allways true.");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void configParsers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(RowDataDTO r, int threadID) throws IOException {
		// TODO Auto-generated method stub

	}

	public void write(RowDataDTO r) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush(int workerID) throws IOException {
		// TODO Auto-generated method stub

	}

}
