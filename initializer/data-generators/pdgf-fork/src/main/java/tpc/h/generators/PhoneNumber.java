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
public class PhoneNumber extends Generator {

	private final static int NATIONS_COUNT = 24 + 1;
	private final static int COUNTRY_CODE_ADD = 10;
	private final static int LOCAL_NUM_1_2_MIN = 100;
	private final static int LOCAL_NUM_1_2_MAX = 999;
	private final static int LOCAL_NUM_1_2_SPREAD = LOCAL_NUM_1_2_MAX
			- LOCAL_NUM_1_2_MIN + 1;
	private final static int LOCAL_NUM_3_MIN = 1000;
	private final static int LOCAL_NUM_3_MAX = 9999;
	private final static int LOCAL_NUM_3_SPREAD = LOCAL_NUM_3_MAX
			- LOCAL_NUM_3_MIN + 1;
	private final static char SEPERATOR = '-';

	private StringBuilder text;

	/*
	 * private ThreadLocal<StringBuilder> perThreadStringBuilder = new
	 * ThreadLocal<StringBuilder>() {
	 * 
	 * @Override protected StringBuilder initialValue() {
	 * 
	 * return new StringBuilder();
	 * 
	 * } };
	 */

	public PhoneNumber() throws ConfigurationException, XmlException {
		super(
				"The term phone number represents a string of numeric characters separated by hyphens and generated as follows:"
						+ "\nLet i be an index into the list of strings Nations (i.e., ALGERIA is 0, ARGENTINA is 1, etc., see Clause 4.2.3),"
						+ "\nLet country_code be the sub-string representation of the number (i + 10),"
						+ "\nLet local_number1 be random [100 .. 999],"
						+ "\nLet local_number2 be random [100 .. 999],"
						+ "\nLet local_number3 be random [1000 .. 9999],"
						+ "\nThe phone number string is obtained by concatenating the following sub-strings:"
						+ "\ncountry_code, \"-\", local_number1, \"-\", local_number2, \"-\", local_number3");
		text = new StringBuilder();
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		// StringBuilder text = perThreadStringBuilder.get();
		text.setLength(0);
		// text.delete(0, text.length());

		text.append((rng.nextInt(NATIONS_COUNT) + COUNTRY_CODE_ADD));
		text.append(SEPERATOR);
		text.append((rng.nextInt(LOCAL_NUM_1_2_SPREAD) + LOCAL_NUM_1_2_MIN));
		text.append(SEPERATOR);
		text.append((rng.nextInt(LOCAL_NUM_1_2_SPREAD) + LOCAL_NUM_1_2_MIN));
		text.append(SEPERATOR);
		text.append((rng.nextInt(LOCAL_NUM_3_SPREAD) + LOCAL_NUM_3_MIN));
		currentFieldValue.setValue(text.toString());
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
