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
import pdgf.core.dbSchema.Field;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;
import pdgf.util.File.LineAccessFile;

public class NameGenerator extends Generator {

	private Field genderField = null;
	private LineAccessFile male;
	private LineAccessFile female;

	public NameGenerator() throws XmlException {
		super(
				"First file must provide a list of Male names, second file must provide a list of female names");
		// TODO Auto-generated constructor stub

	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);

		// check if name should depend on a referenced GenderGenerator
		if (this.getParent().getReference(0) != null) {
			genderField = this.getParent().getReference(0).getRefField();
			if (!(genderField.getChild() instanceof GenderGenerator)) {
				throw new XmlException(getNodeInfo()
						+ "Generator of reference: "
						+ genderField.getNodeInfo()
						+ " must be an instance of GenderGenerator");
			}
		}
		male = getFile(0);
		female = getFile(1);
	}

	@Override
	protected void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		boolean isMale = false;

		if (genderField == null) {
			isMale = rng.nextBoolean();
		} else {
			isMale = ((Character) getCachedValue(genderField, generationContext)
					.getCachedValue() == 'M');
		}

		if (isMale) {
			currentFieldValue.setValue(male.getLine(getRandomNo(rng,
					generationContext, male.getLineCount())));
		} else {
			currentFieldValue.setValue(female.getLine(getRandomNo(rng,
					generationContext, female.getLineCount())));

		}
	}

	private long getRandomNo(AbstractPDGFRandom rng, GenerationContext gc,
			long l) {

		long number;
		if (getDistribution() == null) {
			number = rng.nextLong();
		} else {
			number = getDistribution().nextLongValue(rng, gc);

		}
		if (number < 0) {
			number = -number;
		}
		return number % l;

	}

	@Override
	protected void configParsers() throws XmlException {
		// TODO Auto-generated method stub
		super.configParsers();
		addFileNodeParser();
	}

}
