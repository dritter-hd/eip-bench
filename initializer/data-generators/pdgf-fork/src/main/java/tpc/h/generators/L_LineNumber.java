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
package tpc.h.generators;

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Generator;
import pdgf.plugin.AbstractPDGFRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class L_LineNumber extends Generator {
	private static int chunkSize;

	private static int[] l_lineNumber;
	static {
		// 1 + ((7-1)/2) = 4 = mean number of rows.
		chunkSize = 4 * 7;
		l_lineNumber = new int[chunkSize];
		int pos = 0;

		/*
		 * generates l_Linenumber
		 * {1,1,2,1,2,3,1,2,3,4,1,2,3,4,5,1,2,3,4,5,6,1,2,3,4,5,6,7}
		 */
		for (int i = 1; i <= 7; i++) {
			for (int j = 1; j <= i; j++) {
				l_lineNumber[pos] = j;
				pos++;
				// System.out.println(i + " " + j);
			}
		}
	}

	public L_LineNumber() throws ConfigurationException, XmlException {
		super("unique within [1, 7] per line in orders table");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		currentFieldValue.setValue(calcLineNumberForRow(generationContext
				.getCurrentRow())); //.getID() equals getCurRow() for historical tables

	}

	public static int calcLineNumberForRow(long row) {
		return l_lineNumber[(int) ((row - 1) % chunkSize)];
	}

	@Override
	protected void configParsers() throws XmlException{
		super.configParsers();
	}

	@Override
	public void initialize(int workers) throws ConfigurationException,
			XmlException {
		super.initialize(workers);
	}

}
