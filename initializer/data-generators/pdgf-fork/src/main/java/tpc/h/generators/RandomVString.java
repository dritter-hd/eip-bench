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
import pdgf.util.random.PdgfDefaultRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class RandomVString extends Generator {
	private final static int SPREAD_MIN = 4;
	private final static int SPREAD_MAX = 16;
	private final static int SPREAD = SPREAD_MAX - SPREAD_MIN;

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

	public RandomVString() throws ConfigurationException, XmlException {
		super(
				"The notation random v-string [x] represents a string comprised of randomly generated alphanumeric characters within a character set of at least 64 symbols. The length of the string is a random value [0.4 x .. 1.6 x] rounded up to the next integer.");

	}

	public static void main(String[] args) throws ConfigurationException,
			XmlException {

		RandomVString t = new RandomVString();

		t.setSize(50);
		t.setRngName("pdgf.util.random.PdgfDefaultRandom");
		t.initialize(1);
		FieldValueDTO fwdto = new FieldValueDTO(java.sql.Types.INTEGER, null);
		PdgfDefaultRandom rng = new PdgfDefaultRandom();
		GenerationContext gc = new GenerationContext();
		System.out.println(new char[] { 'a', 'b' });
		for (int i = 1; i < 100; i++) {
			gc.setCurrentRow(i);
			gc.setID(i);
			t.getNextValue(rng, gc, fwdto);
			if (fwdto.getValue().getClass().isArray()) {

				if (fwdto.getValue() instanceof char[]) {
					System.out.println(i + ": "
							+ new String(((char[]) fwdto.getValue())));
				}

			} else {
				System.out.println(i + ": " + fwdto.getValue());
			}
		}
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();
		
		getNodeParser(NODE_PARSER_size).setRequired(true);
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		int len = (int) getSize() * (SPREAD_MIN + rng.nextInt(SPREAD + 1));
		len = (len % 10) == 0 ? len / 10 : (len / 10) + 1;
		char[] value = new char[len];

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
