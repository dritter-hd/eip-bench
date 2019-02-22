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

import org.w3c.dom.Node;

import pdgf.core.FieldValueDTO;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;

/**
 * 
 * A very simple ID Generator. As id the current Row number is used. Does not
 * support any Distribution
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 * 
 */
public class StaticValueGenerator extends Generator {

	public static final String NODE_PARSER_value = "value";
	private char[] value = null;

	public StaticValueGenerator() throws XmlException {
		super("For all rows the same value is used as specified in <"
				+ NODE_PARSER_value + ">The value to be used</"
				+ NODE_PARSER_value + ">");
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		// set next value result
		currentFieldValue.setValue(value);

	}

	protected void configParsers() throws XmlException {
		super.configParsers();
		addNodeParser(new ValueNodeParser(true, true, this));
		this.getNodeParser(this.NODE_PARSER_distribution).setRequired(false)
				.setUsed(false);

	}

	private class ValueNodeParser extends Parser<Generator> {

		public ValueNodeParser(boolean required, boolean used, Generator parent) {
			super(required, used, NODE_PARSER_value, parent,
					"The static value to be used for all rows.");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String nodeText = null;
			if (node == null || (nodeText = node.getTextContent()) == null
					|| nodeText.isEmpty()) {
				if (this.isRequired()) {
					throw new XmlException(getNodeInfo() + "<" + this.getName()
							+ "> must not be empty.");
				}
			} else {
				value = nodeText.toCharArray();
			}
		}
	}
}
