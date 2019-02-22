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
public class RandomAString extends Generator {
	// Max is 127! any additional chars will not be selected by the algorithm
	private static char[] CHARACTER_SET = new char[68];

	static {
		int pos = 0;

		for (int i = 0; i < ('Z' - '0' + 1); i++) {
			CHARACTER_SET[pos++] = (char) ('0' + i);
		}

		for (int i = 0; i < 'z' - 'a'; i++) {
			CHARACTER_SET[pos++] = (char) ('a' + i);
		}

	}

	public RandomAString() throws ConfigurationException, XmlException {
		super(
				"The notation random a-string [x] represents a string of length x comprised of randomly generated alphanumeric characters within a character set of at least 64 symbols.");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void configParsers()throws XmlException {
		super.configParsers();
		
		getNodeParser(NODE_PARSER_size).setRequired(true);
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		char[] value = new char[(int) getSize()];

		// method 1, very slow!!
		// for (int i = 0; i < value.length; i++) {
		// value[i] = CHARACTER_SET[rng.nextInt(CHARACTER_SET.length)];
		// }

		// method2
		byte[] randVals = new byte[value.length];
		rng.nextBytes(randVals);

		for (int i = 0; i < value.length; i++) {
			// Bitmask 0x7F = 01111111, so first bit will allays be 0 which
			// reduces possible int values to only positives from [0,127]
			value[i] = CHARACTER_SET[(randVals[i] & 0x7F)
					% CHARACTER_SET.length];
		}

		currentFieldValue.setValue(value);

	}

	@Override
	public void initialize(int workers) throws ConfigurationException,
			XmlException {
		super.initialize(workers);
		
	}

}
