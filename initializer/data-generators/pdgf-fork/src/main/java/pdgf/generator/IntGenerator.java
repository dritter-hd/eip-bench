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
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;

/**
 * Generates an Integer Value beween &lt;min> and &lt;max>
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 * 
 */
public class IntGenerator extends Generator {

	public IntGenerator() throws XmlException {
		super(
				"Generates an integer value between <min> and <max>, distributed as spcified in <distribution>");
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();

		getNodeParser(NODE_PARSER_min).setRequired(true);
		getNodeParser(NODE_PARSER_max).setRequired(true);
		getNodeParser(NODE_PARSER_distribution)
				.setRequired(false)
				.setUsed(true)
				.setDescription(
						"Distribution is supportet but not neede. "
								+ getNodeParser(NODE_PARSER_distribution)
										.getDescription());

	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		long part = getMax() - getMin() + 1;

		if (getDistribution() == null) {
			if (part < Integer.MAX_VALUE) {
				// set next value result
				currentFieldValue.setValue((getMin() + rng.nextInt() % part));
			} else {
				// set next value result
				currentFieldValue.setValue((getMin() + rng.nextInt() % part));
			}
		} else {

			if (part < Integer.MAX_VALUE) {
				// set next value result
				currentFieldValue.setValue((getMin() + this.getDistribution()
						.nextIntValue(rng, generationContext) % part));
			} else {
				// set next value result
				currentFieldValue.setValue((getMin() + this.getDistribution()
						.nextLongPositiveValue(rng, generationContext) % part));
			}

		}
	}

}
