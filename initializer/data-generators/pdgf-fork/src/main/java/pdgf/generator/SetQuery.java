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
package pdgf.generator;

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;

/**
 * 
 * Generates the SetQuery result for the current Field &lt;File>
 * 
 * @author Michael Frank
 * @version 1.0 16.12.2009
 * 
 */
public class SetQuery extends Generator {

	public SetQuery() throws XmlException {
		super(
				"Generates fields K500K, K250K, K100K, K40K, K10K,  K1K, K100, K25 K10, K5, K4 and K2 of the set query benchmark. For KSEQ the IdGenerator is used  and for S fields the StaticValueGenerator. Does not support use of differnt distributions");
	}

	private int localColCardValue = -1;

	private final static int[] colCard = { 500000, 250000, 100000, 40000,
			10000, 1000, 100, 25, 10, 5, 4, 2 };

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);
		localColCardValue = colCard[this.getParent().getElementID() - 1]; /*-1 because first element is KSEQ, a sequenzenumber */
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		// long version
		// int currentColCardNo = this.getParent().getElementID();
		// int colCardVal = colCard[currentColCardNo];
		//
		// int seed = rng.nextInt();
		// int value = (seed % colCardVal) + 1;
		// currentFieldValue.setValue(value);

		// alternativ
		// int colCardVal = colCard[this.getParent().getElementID()-1];
		// currentFieldValue.setValue(rng.nextInt(colCardVal)+1);

		// fastest
		currentFieldValue.setValue((rng.nextInt(localColCardValue) + 1));
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();

	}

}
