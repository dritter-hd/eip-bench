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
public class Ps_Suppkey extends Generator {
	private long scaleFactor;

	public Ps_Suppkey() throws ConfigurationException, XmlException {
		super(
				"For each row in part table, four rows in PARTSUP table with PS_PARTKEY = P_PARTKEY and PS_SUPPKEY = (PS_PARTKEY + (i * (( S/4 ) + (int)(PS_PARTKEY-1 )/S)))) modulo S + 1 where i is the ith supplier within [0 .. 3] and S = SF * 10,000.");
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);
		

		scaleFactor = (long) (getParent().getParent().getParent().getScaleFactor() * 10000);
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		// what it does:

		currentFieldValue.setValue(calcPs_SuppkeyforRow(generationContext
				.getID()));//getID == getCurRow for historical tables

	}

	/**
	 * PS_SUPPKEY = (PS_PARTKEY + (i * (( S/4 ) + (int)(PS_PARTKEY-1 )/S))))
	 * modulo S + 1 where i is the ith supplier within [0 .. 3] and S = SF *
	 * 10,000.
	 * 
	 * For each row in part table, four rows in PARTSUP table with PS_PARTKEY =
	 * P_PARTKEY<br/>
	 *row P_PARTKEY<br/>
	 * (1 -1)/4 +1 = 1<br/>
	 * (2 -1)/4 +1 = 1<br/>
	 * (3 -1)/4 +1 = 1<br/>
	 * (4 -1)/4 +1 = 1<br/>
	 * (5 -1)/4 +1 = 2<br/>
	 * (6 -1)/4 +1 = 2<br/>
	 * (7 -1)/4 +1 = 2<br/>
	 * (8 -1)/4 +1 = 2<br/>
	 * (9 -1)/4 +1 = 3<br/>
	 * (10-1)/4 +1 = 3<br/>
	 * ...
	 * 
	 * @param suppkey
	 *            for current row
	 * @param ps_partkey
	 * @return
	 */
	public long calcPs_SuppkeyforRow(long row) {
		long i = (row - 1) % 4; // [0,3]
		long ps_partkey = Ps_Partkey.calcPsPartkeyFromRow(row);
		return (ps_partkey + (i * ((scaleFactor / 4) + (int) (ps_partkey - 1)
				/ scaleFactor)))
				% scaleFactor + 1;
	}

	@Override
	protected void configParsers()throws XmlException {
		super.configParsers();
		
	}

}
