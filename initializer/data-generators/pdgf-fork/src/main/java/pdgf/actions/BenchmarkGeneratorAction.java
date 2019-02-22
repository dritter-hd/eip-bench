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
import pdgf.core.exceptions.InvalidStateException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;

public class BenchmarkGeneratorAction extends Action {

	public BenchmarkGeneratorAction() {
		super(
				"benchGen",
				"{<TableName> <FieldName> <time>(optional)} OR {ALL <time>}\n",
				"Benchmarks a generator for a specified field in a table (must be configured via xml) or if the first parameter equals \"ALL\", generators of all fields and tables are benchmarkt. For both uses you can specified the  <time> in ms a benchmark should run",
				2, 3);
	}

	@Override
	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException, ConfigurationException,
			InvalidStateException {
		checkParamQuantity(tokens);
		checkStartPreconditions();
		initialize();

		long benchTime = 3 * 1000; // default Benchmarktime
		if (tokens.length >= 4 && tokens[3] != null && !tokens[3].isEmpty()) {
			benchTime = Long.parseLong(tokens[3]);
		} else if (tokens[1].equals("ALL") && tokens[2] != null
				&& !tokens[2].isEmpty()) {
			benchTime = Long.parseLong(tokens[2]);
		}

		if (tokens[1].equals("ALL")) {
			Table[] tables = project.getChilds();
			for (Table table : tables) {
				Field[] fields = table.getChilds();
				for (Field field : fields) {
					benchmarkAGenerator(benchTime, field);
				}
			}
		} else {
			String table = tokens[1];
			String field = tokens[2];

			benchmarkAGenerator(table, field, benchTime);
		}
	}

	private boolean benchmarkAGenerator(String table, String field,
			long benchTime) {
		Table t = project.getChild(table);
		if (t == null) {
			log.info("ERROR! Command: \"" + command
					+ "\" cannot be executed because: table " + table
					+ " does not exist");
			return false;
		}

		Field f = t.getChild(field);
		if (f == null) {

			log.info("ERROR! Command: \"" + command
					+ "\" cannot be executed because: field " + field
					+ " does not exist");
			return false;
		}

		benchmarkAGenerator(benchTime, f);
		return true;
	}

	private void benchmarkAGenerator(long benchTime, Field f) {
		// DecimalFormat df = (DecimalFormat)
		// DecimalFormat.getInstance(Locale.US);
		// df.applyPattern("#,###,##0");

		AbstractPDGFRandom rng = project.getNewElementRng(project.getSeed());
		GenerationContext gc = new GenerationContext(1, 1, project);
		FieldValueDTO fv = f.getNewFieldValueDTO();

		long i = 1;
		long bytes = 0;
		log.info(f.getParent().getName() + ":" + f.getName() + "-> "
				+ f.getGenerator(1).getName() + ";");
		long start = System.currentTimeMillis();

		while ((start + benchTime) > System.currentTimeMillis()) {
			gc.setCurrentRow(i++);
			f.getGenerator(1).getNextValue(rng, gc, fv);
			bytes += countBytes(fv.getValue());
		}
		log.info(((i * 1000) / benchTime) + ";vals/s;");
		log.info((((bytes * 1000) / 1024) / benchTime) + ";Kbyte/s");
	}

	private int countBytes(Object value) {
		if (value instanceof char[]) {
			return ((char[]) value).length;
		} else {
			return value.toString().length();
		}

	}
}
