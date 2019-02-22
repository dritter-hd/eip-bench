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
import pdgf.util.StaticHelper;

/**
 * Generates a double value between minD and maxD with the provided number of
 * decimalPlaces
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class DoubleGenerator extends Generator {
	public static final String NODE_PARSER_minD = "minD";
	public static final String NODE_PARSER_maxD = "maxD";
	public static final String NODE_PARSER_decimalPlaces = "decimalPlaces";

	private double minD;
	private double maxD;
	private int decimalPlaces;

	public DoubleGenerator() throws XmlException {
		super("Generates real double values between min and max");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		if (getDistribution() != null) {
			currentFieldValue.setValue(StaticHelper.roundDouble((minD + this
					.getDistribution().nextDoubleValue(rng, generationContext)
					* (maxD - minD)), decimalPlaces));
		} else {
			currentFieldValue.setValue(StaticHelper.roundDouble(
					(minD + rng.nextDouble() * (maxD - minD)), decimalPlaces));
		}

		/*
		 * below for understanding a more readable form of the code above double
		 * 
		 * randDouble = this.getDistribution().nextDoubleValue(rng,
		 * generationContext); double scaledDouble = minD +
		 * randDouble*(maxD-minD); //limit to specified number of decimalPlaces
		 * scaledDouble = StaticHelper.roundDouble(scaledDouble, decimalPlaces);
		 * currentFieldValue.setValue(scaledDouble);
		 */

	}

	public void setMinD(double mind) {
		this.minD = mind;
	}

	public double getMind() {
		return minD;
	}

	public void setMaxD(double maxd) {
		this.maxD = maxd;
	}

	public double getMaxd() {
		return maxD;
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();
		getNodeParser(NODE_PARSER_distribution).setRequired(false)
				.setUsed(true);
		addNodeParser(new MinDParser(true, true, this));
		addNodeParser(new MaxDParser(true, true, this));
		addNodeParser(new DecimalPlacesParser(true, true, this));
	}

	private class MinDParser extends Parser<DoubleGenerator> {

		public MinDParser(boolean isRequired, boolean used,
				DoubleGenerator parent) {
			super(isRequired, used, NODE_PARSER_minD, parent,
					"minimal oputput value of this generator.");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				setMinD(StaticHelper
						.parseDoubleTextContent(getNodeInfo(), node));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getNodeInfo());
					errMsg.append('<');
					errMsg.append(node.getNodeName());
					errMsg.append("> must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">-4.33</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}
		}
	}

	private class MaxDParser extends Parser<DoubleGenerator> {

		public MaxDParser(boolean isRequired, boolean used,
				DoubleGenerator parent) {
			super(isRequired, used, NODE_PARSER_maxD, parent,
					"maximal oputput value of this generator.");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				setMaxD(StaticHelper
						.parseDoubleTextContent(getNodeInfo(), node));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getNodeInfo());
					errMsg.append('<');
					errMsg.append(node.getNodeName());
					errMsg.append("> must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">41.67</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}
		}
	}

	private class DecimalPlacesParser extends Parser<DoubleGenerator> {

		public DecimalPlacesParser(boolean isRequired, boolean used,
				DoubleGenerator parent) {
			super(
					isRequired,
					used,
					NODE_PARSER_decimalPlaces,
					parent,
					"number of decimal places of output. Example: places=2 -> output: 3.64; places=3 -> 3.642");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				setDecimalPlaces(StaticHelper.parseIntTextContent(
						getNodeInfo(), node, 0, Integer.MAX_VALUE));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getNodeInfo());
					errMsg.append('<');
					errMsg.append(node.getNodeName());
					errMsg.append("> must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">2</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}
		}
	}
}
