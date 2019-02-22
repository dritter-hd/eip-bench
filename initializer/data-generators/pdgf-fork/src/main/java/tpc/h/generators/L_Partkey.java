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
public class L_Partkey extends Generator {

	private static final int rowsPartTable = 200000;
	private long scaled_rows_PartTable;

	private long lastValue = 0;

	public L_Partkey() throws ConfigurationException, XmlException {
		super("random value [1, (SF*200000)]");
		// TODO Auto-generated constructor stub
	}

	/*
	 * // a Last value buffer per thread private ThreadLocal<Long>
	 * perThreadLastValue = new ThreadLocal<Long>() {
	 * 
	 * @Override protected Long initialValue() {
	 * 
	 * return new Long(0); } };
	 */
	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);
		
		scaled_rows_PartTable = (long) (this.getParentField().getParent().getParent().getScaleFactor()
		* rowsPartTable);
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		long value = rng.nextLong();
		// make positive
		if (value < 0) {
			value = -value;
		}

		value = 1 + rng.nextLong() % scaled_rows_PartTable; // scale to [1,
		// (SF*200.000)]

		// perThreadLastValue.set(value); //buffer value for L_Suppkey
		lastValue = value;
		currentFieldValue.setValue(value);

	}

	public Long getLastValue() {
		return lastValue;
		// return perThreadLastValue.get();
	}
	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();
		
	}

}
