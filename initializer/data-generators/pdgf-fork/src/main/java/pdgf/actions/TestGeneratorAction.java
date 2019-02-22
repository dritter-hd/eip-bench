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
package pdgf.actions;

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.dbSchema.Field;
import pdgf.core.dbSchema.Table;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.InvalidStateException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;

public class TestGeneratorAction extends Action {

	public TestGeneratorAction() {
		super(
				"testgen",
				"<TableName> <FieldName> <iterations>(optional)\n",
				"Tests the generator (configured via xml) for the specified  field in specified table <iteration> times and ouputs his generated value",
				2, 3);
	}

	@Override
	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException, InvalidElementException,
			ConfigurationException, InvalidStateException {
		checkParamQuantity(tokens);
		checkStartPreconditions();
		dataGen.initialize(project);

		String table = tokens[1];
		String field = tokens[2];
		int iterations = 10;

		if (tokens.length >= 4 && tokens[3] != null && !tokens[3].isEmpty()) {
			iterations = Integer.parseInt(tokens[3]);
		}

		Table t = project.getChild(table);
		if (t == null)
			throw new InvalidElementException("ERROR! Command: \""
					+ new TestGeneratorAction().getCommand()
					+ "\" cannot be executed because: table " + table
					+ " does not exist");

		Field f = t.getChild(field);
		if (f == null)
			throw new InvalidElementException("ERROR! Command: \""
					+ new TestGeneratorAction().getCommand()
					+ "\" cannot be executed because: field " + field
					+ " does not exist");

		AbstractPDGFRandom rng = project.getNewElementRng(project.getSeed());
		GenerationContext gc = new GenerationContext(1, 1, project);
		FieldValueDTO fv = f.getNewFieldValueDTO();

		for (int i = 1; i < 1 + iterations; i++) {
			gc.setCurrentRow(i);
			f.getGenerator(1).getNextValue(rng, gc, fv);

			if (fv.getValue() instanceof char[]) {
				log.info("value was in a char[]");
				log.info(i + ": " + new String(((char[]) fv.getValue())));
			} else {
				log.info(i + ": " + fv.getValue().toString());
			}
		}
	}
}
