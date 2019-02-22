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
import pdgf.core.dbSchema.Field;
import pdgf.core.dbSchema.Reference;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;

/**
 * Default reference generator
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 * 
 */
public class DefaultReferenceGenerator extends Generator {
	public static final String NODE_PARSER_disableState = "disableState";
	public static final String NODE_PARSER_sameRowAs = "sameRowAs";

	private String sameRowAsGeneratorFieldName = null;
	private Field sameRowAsGenerator = null;

	private Reference r = null;

	public DefaultReferenceGenerator() throws XmlException {
		super(
				"Generates values for referencing field by following the reference and randomly (following the specified distribution) picking a row from the value set (rows) of the referenced Table and recalculating its Value.");
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);

		r = this.getParent().getReference(0);
		if (r == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " no reference defined for parent field! You can not use this Generator if reference is not defined.");
		}

		if (sameRowAsGeneratorFieldName != null
				&& !sameRowAsGeneratorFieldName.isEmpty()) {
			Field f = this.getParent().getParent()
					.getChild(sameRowAsGeneratorFieldName);
			if (f == null) {
				throw new XmlException(getNodeParser(NODE_PARSER_sameRowAs)
						.getParserNodeInfo()
						+ " Field: "
						+ sameRowAsGeneratorFieldName + " not found.");
			}
			sameRowAsGenerator = f;

		}

	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		long referencedRow;

		if (sameRowAsGenerator != null) {

			referencedRow = getCachedValue(sameRowAsGenerator,
					generationContext).getLastRandomRow();

		} else {
			referencedRow = randomNewRow(rng, generationContext,
					r.getRefTableSize());

		}
		r.getReferencedValue(referencedRow, generationContext,
				currentFieldValue);

		getCachedValue().setLastRandomRow(referencedRow);

	}

	/**
	 * returns a row number between [1 ,rowsPrimTable]
	 * 
	 * @param rng
	 * @param generationContext
	 * @param rowsPrimTable
	 * @return
	 */
	private long randomNewRow(AbstractPDGFRandom rng,
			GenerationContext generationContext, long rowsPrimTable) {
		if (this.getDistribution() != null) {

			return 1
					+ this.getDistribution().nextLongPositiveValue(rng,
							generationContext) % rowsPrimTable;
		} else {
			long rand = rng.nextLong();
			if (rand < 0) {
				rand = -rand;
			}
			return 1 + rand % rowsPrimTable;
		}
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();
		getNodeParser(NODE_PARSER_distribution).setRequired(false)
				.setUsed(true);
		addNodeParser(new SameRowAsParser(false, true, this));

	}

	private class SameRowAsParser extends Parser<Generator> {

		public SameRowAsParser(boolean isRequired, boolean used,
				Generator parent) {
			super(isRequired, used, NODE_PARSER_sameRowAs, parent, "");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			sameRowAsGeneratorFieldName = node.getTextContent();
		}
	}
}
