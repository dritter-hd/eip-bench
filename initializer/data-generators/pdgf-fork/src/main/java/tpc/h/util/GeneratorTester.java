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
package tpc.h.util;

import java.io.IOException;
import java.util.Random;

import pdgf.Controller;
import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.DataGenerator;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.generator.DictList;
import pdgf.generator.DoubleGenerator;
import pdgf.generator.IdGenerator;
import pdgf.generator.IntGenerator;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.util.random.PdgfDefaultRandom;
import tpc.h.generators.PhoneNumber;
import tpc.h.generators.RandomAString;
import tpc.h.generators.RandomValueXY;
import tpc.h.generators.TextString;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class GeneratorTester {
	TextString ts2;
	RandomValueXY rvxy;
	DictList dl;
	IntGenerator ig;
	IdGenerator id;
	RandomAString ras;
	PhoneNumber phone;
	DoubleGenerator duble;

	GeneratorTester() throws IOException, ConfigurationException, XmlException {
		id = new IdGenerator();

		ig = new IntGenerator();
		ig.setMin(-10);
		ig.setMax(1000000000);

		ts2 = new TextString();
		ts2.setSize(124);

		rvxy = new RandomValueXY();
		rvxy.setX_I(-99999);
		rvxy.setY_I(999999);
		rvxy.setDecimalPlaces(2);

		duble = new DoubleGenerator();
		duble.setMinD(-999.99);
		duble.setMaxD(9999.99);
		duble.setDecimalPlaces(2);

		dl = new DictList();
		//dl.setFilePath("dicts/tpc-h/region.dict");
		dl.setSize(4);
		dl.setSeperator("|foo|");
		// dl.setUnique(true);

		ras = new RandomAString();
		ras.setSize(124);

		phone = new PhoneNumber();
	}

	Controller c;

	public static void main(String[] args) throws IOException, XmlException,
			ConfigurationException {
		GeneratorTester gt = new GeneratorTester();

		gt.start();

	}

	private void start() throws IOException, XmlException,
			ConfigurationException {

		// create necessary context for Generators to operate under "real world"
		// conditions
		c = new Controller();
	//	c.executeCommand(Controller.Command.loadConfig, "",				"config/paperSchema.xml");
		//c.executeCommand(Controller.Command.loadConfig, "", "config/node.xml");
		// c.executeCommand(Controller.Command.closeWhenDone, "");
		//c.initialize();
		DataGenerator dg = c.getDataGenerator();
		PdgfDefaultRandom rng = new PdgfDefaultRandom();
		Random r = new Random(7857575278438974L);
		pdgf.core.dataGenerator.GenerationContext gc = new pdgf.core.dataGenerator.GenerationContext();
		gc.set(10, 100, 0, 100, c.getProject(), 1, c.getProject().getWorkers());
		FieldValueDTO fwDTO = new FieldValueDTO(null, java.sql.Types.VARCHAR,
				null);

		long end, start;

		//		
		// byte [] b = new byte[10000000];
		//		
		// start = System.currentTimeMillis();
		// for (int i = 0; i < 100; i++) {
		// r.nextBytes(b);
		//			
		// }
		//		
		// System.out.println(System.currentTimeMillis()-start);
		// start = System.currentTimeMillis();
		// for (int i = 0; i < 100; i++) {
		// rng.nextBytes(b);
		// }
		// System.out.println(System.currentTimeMillis()-start);

		System.out.println("\nStart Generator test:\n");

		// TextString pesudotext generaotr
		// testTextStringGenerator(rng, gc, fwDTO);
		// System.out.println("GeneratorOutput:\n" + fwDTO.value.toString());

		// DictList generator
		// dictListGenerator(rng, gc, fwDTO);

		System.out.println("GeneratorOutput:\n");

		// for (int j = 0; j < 100; j++) {
		//			
		// randomValueXYGenerator(rng, gc, fwDTO);
		//		
		rvxy.getNextValue(rng, gc, fwDTO);
		if (fwDTO.getValue() instanceof char[]) {

			char[] tmp = (char[]) fwDTO.getValue();
			System.out.println("Length :" + tmp.length + " : ");
			for (int i = 0; i < tmp.length; i++) {
				System.out.print(tmp[i]);
			}
			System.out.println();
		} else {
			System.out.println("len: " + fwDTO.getValue().toString().length()
					+ " : " + fwDTO.getValue().toString());
		}
		//			
		// }
		char[] buf = new char[4 * 1024 * 1024];
		// benchmark generator
		int count = 10000000;
		start = System.currentTimeMillis();
		String tmp;
		for (int j = 0; j < count; j++) {
			// randomValueXYGenerator(rng, gc, fwDTO);
			// dictListGenerator(rng, gc, fwDTO);
			// testTextStringGenerator(rng, gc, fwDTO);
			// ig.getNextValue(rng, gc, fwDTO);
			// id.getNextValue(rng, gc, fwDTO);
			duble.getNextValue(rng, gc, fwDTO);
			// System.out.println(fwDTO.value.toString());#

			// simulate output because of conversion and copy times
			if (fwDTO.getValue() instanceof char[]) {
				System.arraycopy((char[]) fwDTO.getValue(), 0, buf, 0,
						((char[]) fwDTO.getValue()).length);
			} else {
				tmp = fwDTO.getValue().toString();
				tmp.getChars(0, tmp.length(), buf, 0);
			}

		}
		end = System.currentTimeMillis();
		System.out.println((count / (end - start) * 1000) + " Values/s Time: "
				+ (end - start) + "ms");
	}

	private void randomValueXYGenerator(AbstractPDGFRandom rng,
			pdgf.core.dataGenerator.GenerationContext generationContext, FieldValueDTO currentFieldValue)
			throws IOException {

		rvxy.getNextValue(rng, generationContext, currentFieldValue);

	}

	private void dictListGenerator(AbstractPDGFRandom rng,
			pdgf.core.dataGenerator.GenerationContext generationContext, FieldValueDTO currentFieldValue)
			throws IOException {

		dl.getNextValue(rng, generationContext, currentFieldValue);

	}

	private void testTextStringGenerator(AbstractPDGFRandom rng,
			pdgf.core.dataGenerator.GenerationContext generationContext, FieldValueDTO currentFieldValue)
			throws IOException {

		ts2.getNextValue(rng, generationContext, currentFieldValue);

	}
}
