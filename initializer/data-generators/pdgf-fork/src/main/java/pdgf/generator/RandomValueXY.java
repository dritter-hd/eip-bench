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

import pdgf.core.Element;
import pdgf.core.FieldValueDTO;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class RandomValueXY extends Generator {
	public static final int TYPE_IS_STRING = 0;
	public static final int TYPE_IS_LONG = 1;

	public static final String NODE_PARSER_X = "x";
	public static final String NODE_PARSER_Y = "y";
	public static final String NODE_PARSER_CacheLastValue = "cacheLastValue";

	private boolean cache = false;

	private long x; // x part of [x .. y] representing a random value between x
	// and y
	private long y;// y part of [x .. y] representing a random value between x
	// and y
	private int decimalPlaces = Constants.INT_NOT_SET; // number of digits of

	private CacheEntry lastValue = null;

	// private long lastValue =0;
	// precision as shown.
	// For example, [0.01 ..
	// 100.00]

	public RandomValueXY() throws XmlException {
		super(
				"The notation random value [x .. y] represents a random value between x and y inclusively, with a mean of (x+y)/2, and with the same number of digits of precision as shown. For example, [0.01 .. 100.00] has 10,000 unique values, whereas [1..100] has only 100 unique values.");

	}

	/*
	 * // a Last value buffer per thread private ThreadLocal<CacheEntry>
	 * perThreadLastValue = new ThreadLocal<CacheEntry>() {
	 * 
	 * @Override protected CacheEntry initialValue() {
	 * 
	 * return new CacheEntry(); } };
	 */
	@Override
	protected void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		long randValue;
		if (getDistribution() != null) {
			randValue = getDistribution().nextLongPositiveValue(rng,
					generationContext);
		} else {
			randValue = rng.nextLong();
		}
		if (randValue < 0) {
			randValue = -randValue;
		}
		randValue = x + randValue % (y - x + 1);

		currentFieldValue.setPlainValue(randValue);

		if (decimalPlaces == Constants.INT_NOT_SET || decimalPlaces < 1) {
			currentFieldValue.setValue(randValue);
			if (cache) {
				// CacheEntry cache = perThreadLastValue.get();
				lastValue.setType(TYPE_IS_LONG);
				lastValue.setLastLongVal(randValue);
				// perThreadLastValue.set(cache);
			}
		} else {
			char[] charVal = StaticHelper.longToNumberWithDecimalPlaces(
					randValue, decimalPlaces);

			currentFieldValue.setValue(charVal);
			if (cache) {
				lastValue.setType(TYPE_IS_STRING);
				lastValue.setLastStringVal(charVal);

			}
		}

	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public void setX_I(long x_I) {
		this.x = x_I;
	}

	public long getX_I() {
		return x;
	}

	public void setY_I(long y_I) {
		this.y = y_I;
	}

	public long getY_I() {
		return y;
	}

	public boolean CacheLastValueNode() {
		return cache;
	}

	public void setCacheLastValueNode(boolean cache) {
		this.cache = cache;
	}

	public int getLastValueType() {
		return lastValue.getType();
	}

	public long getLastLongType() {
		return lastValue.getLastLongVal();
	}

	public char[] getLastStringType() {
		return lastValue.getLastStringVal();
	}

	@Override
	protected void configParsers() throws XmlException {
		// TODO Auto-generated method stub
		super.configParsers();
		addNodeParser(new XNodeParser(true, true, this));
		addNodeParser(new YNodeParser(true, true, this));
		addNodeParser(new CacheLastValueNodeParser(false, true, this));
	}

	private class XNodeParser extends Parser<Element> {

		public XNodeParser(boolean req, boolean used,
				RandomValueXY randomValueXY) {
			super(
					req,
					used,
					NODE_PARSER_X,
					randomValueXY,
					"X part of [x .. y] represents a random value between x and y inclusively, with a mean of (x+y)/2, and with the same number of digits of precision as shown. For example, [0.01 .. 100.00] has 10,000 unique values");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String tmp = node.getTextContent();
			if (tmp != null && !tmp.isEmpty()) {
				int pointLocation = tmp.indexOf('.');

				// no decimal point, must be integer
				if (pointLocation == -1) {
					setX_I(StaticHelper.parseLongTextContent(getNodeInfo(),
							node, Long.MIN_VALUE, Long.MAX_VALUE));
				} else {
					decimalPlaces = Math.max(decimalPlaces, (tmp.length()
							- pointLocation - 1));

					try {
						setX_I(Long
								.parseLong(tmp.substring(0, pointLocation)
										+ tmp.substring(pointLocation + 1,
												tmp.length())));

					} catch (NumberFormatException e) {
						StringBuilder errMsg = new StringBuilder();
						errMsg.append(getNodeInfo());
						errMsg.append('<');
						errMsg.append(this.getName());
						errMsg.append(">  must be a number between: ");
						errMsg.append(Long.MIN_VALUE);
						errMsg.append(" and ");
						errMsg.append(Long.MAX_VALUE);
						errMsg.append("\n Value was: ");
						errMsg.append(node.getNodeValue());
						throw new XmlException(errMsg.toString());
					}
				}
			} else if (this.isRequired()) {
				throw new XmlException(getNodeInfo() + "<" + this.getName()
						+ "> must not be empty.");
			}

		}

	}

	private class YNodeParser extends Parser<Element> {

		public YNodeParser(boolean b, boolean used, RandomValueXY randomValueXY) {
			super(
					b,
					used,
					NODE_PARSER_Y,
					randomValueXY,
					"Y part of [x .. y] represents a random value between x and y inclusively, with a mean of (x+y)/2, and with the same number of digits of precision as shown. For example, [0.01 .. 100.00] has 10,000 unique values");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String tmp = node.getTextContent();
			if (tmp != null && !tmp.isEmpty()) {
				int pointLocation = tmp.indexOf('.');

				// no decimal point, must be integer
				if (pointLocation == -1) {
					setY_I(StaticHelper.parseLongTextContent(getNodeInfo(),
							node, Long.MIN_VALUE, Long.MAX_VALUE));
				} else {

					decimalPlaces = Math.max(decimalPlaces, (tmp.length()
							- pointLocation - 1));

					try {
						setY_I(Long
								.parseLong(tmp.substring(0, pointLocation)
										+ tmp.substring(pointLocation + 1,
												tmp.length())));

					} catch (NumberFormatException e) {
						StringBuilder errMsg = new StringBuilder();
						errMsg.append(getNodeInfo());
						errMsg.append('<');
						errMsg.append(this.getName());
						errMsg.append(">  must be a number between: ");
						errMsg.append(Long.MIN_VALUE);
						errMsg.append(" and ");
						errMsg.append(Long.MAX_VALUE);
						errMsg.append("\n Value was: ");
						errMsg.append(node.getNodeValue());
						throw new XmlException(errMsg.toString());
					}
				}
			} else if (this.isRequired()) {
				throw new XmlException(getNodeInfo() + "<" + this.getName()
						+ "> must not be empty.");
			}

		}

	}

	private class CacheLastValueNodeParser extends Parser<Element> {

		public CacheLastValueNodeParser(boolean required, boolean used,
				Element parent) {
			super(required, used, NODE_PARSER_CacheLastValue, parent,
					"Cache (per thread) the last generated field value");

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
				cache = Boolean.parseBoolean(nodeText);
			}
		}
	}

	private class CacheEntry {

		private int type = 0;
		private char[] lastStringVal = null;
		private long lastLongVal = 0L;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public char[] getLastStringVal() {
			return lastStringVal;
		}

		public void setLastStringVal(char[] lastStringVal) {
			this.lastStringVal = lastStringVal;
		}

		public long getLastLongVal() {
			return lastLongVal;
		}

		public void setLastLongVal(long lastLongVal) {
			this.lastLongVal = lastLongVal;
		}

	}

}
