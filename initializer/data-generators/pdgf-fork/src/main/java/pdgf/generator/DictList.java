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
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.util.SamplingWithoutReplacement;

/**
 * Takes and returns randomly a line form a dictionary File &lt;File>
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 * 
 */
public class DictList extends Generator {
	public static final String NODE_PARSER_unique = "unique";
	public static final String NODE_PARSER_seperator = "seperator";
	public static final String NODE_PARSER_noRng = "disableRng";
	private boolean unique = false;
	private boolean disabledRng = false;
	private StringBuilder text;
	private SamplingWithoutReplacement urn;

	private String seperator = " "; // default seperator is a single whitespace
	private Object setSizeMutex = new Object();

	/*
	 * private ThreadLocal<StringBuilder> perThreadStringBuilder = new
	 * ThreadLocal<StringBuilder>() {
	 * 
	 * @Override protected StringBuilder initialValue() {
	 * 
	 * return new StringBuilder();
	 * 
	 * } };
	 * 
	 * private ThreadLocal<SamplingWithoutReplacement> perThreadUrn = new
	 * ThreadLocal<SamplingWithoutReplacement>() {
	 * 
	 * @Override protected SamplingWithoutReplacement initialValue() {
	 * 
	 * return new SamplingWithoutReplacement();
	 * 
	 * } };
	 */

	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("foooasfasfasasasasfasfasfo");
		System.out.println(sb.capacity());
		System.out.println(sb.length());
		System.out.println(sb.toString());
		// sb.delete(0, sb.length());
		// sb.replace(0, sb.length(), "");
		sb.setLength(0);
		System.out.println("A:" + sb.toString());
		System.out.println(sb.length());
		System.out.println(sb.capacity());
	}

	public DictList() throws XmlException {
		super(
				"Randomly (using the specified distribution) picking a line from a specified dictionary file. If "
						+ NODE_PARSER_noRng
						+ " is true than lines from file are correlate with rownumber of table (10th row in table = value of 10th row in file. With enabled <"
						+ NODE_PARSER_unique
						+ "> option you can not choose more distinct lines then aviable lines in source file (value of <"
						+ NODE_PARSER_size
						+ "> must not be grater than available lines in specified file");
		setSize(1);// init size before parsing.
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);
		if (getSize() > 1 && disabledRng) {
			throw new XmlException(this.getNodeInfo() + " does not allow <"
					+ NODE_PARSER_noRng + "> to be true while <"
					+ NODE_PARSER_size + "> is > 1");
		}

		if (getSize() > getFile().getLineCount()) {
			throw new XmlException(
					this.getNodeInfo()
							+ " Size was set to: "
							+ getSize()
							+ " which is grater then the number of available lines ("
							+ getFile().getLineCount()
							+ ")in File: "
							+ this.getFile().getFileName()
							+ ". With enabled unique option you can not choose more distinct lines then aviable lines in source file.");
		}

		long size = this.getParent().getSize();
		if (size != Constants.LONG_NOT_SET && size > 0
				&& size < Integer.MAX_VALUE) {
			text = new StringBuilder((int) size);
		} else {
			text = new StringBuilder();
		}

		urn = new SamplingWithoutReplacement();
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		// only one value, no need to concatenate multiple samples with a
		// separator
		if (getSize() > 1) {

			// StringBuilder text = perThreadStringBuilder.get();
			text.setLength(0);

			// has not to be unique, just choose n random lines. available
			if (!unique) {

				text.append(getFile().getLine(
						(int) getRandomNo(rng, generationContext, getFile()
								.getLineCount())));
				for (long i = 1; i < size; i++) {
					text.append(seperator);
					text.append(getFile().getLine(
							(int) getRandomNo(rng, generationContext, getFile()
									.getLineCount())));
				}

			} else {

				/*
				 * take n samples out of m without replacement where n =
				 * getSize() and m is the number of lines in dict file
				 */
				// SamplingWithoutReplacement urn = perThreadUrn.get();
				urn.reset((int) getFile().getLineCount());
				text.append(getFile().getLine(
						urn.takeNextSample(this, rng, generationContext)));
				for (long i = 1; i < size; i++) {
					text.append(seperator);
					text.append(getFile().getLine(
							urn.takeNextSample(this, rng, generationContext)));
				}
			}

			currentFieldValue.setValue(text.toString());
			// System.out.println(currentFieldValue.getValue());

		} else {// size = 1, just choose a random line
			currentFieldValue.setValue(getFile().getLine(
					(int) getRandomNo(rng, generationContext, getFile()
							.getLineCount())));
		}

	}

	/**
	 * returns a random number between 0 (inclusive and max (exclusive)
	 * 
	 * @param rng
	 *            the rng to use
	 * @param gc
	 *            gc for distribution (if needed)
	 * @param l
	 *            the maximum value (exclusive) in the output intervall
	 * @return random value between [0, max[
	 */
	public long getRandomNo(AbstractPDGFRandom rng, GenerationContext gc, long l) {
		if (disabledRng) {
			return gc.getCurrentRow() % l;

		} else {

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
	}

	public void setRngDisabled(boolean disabledRng) {
		this.disabledRng = disabledRng;

	}

	public boolean getRngIsDisabled() {
		return disabledRng;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setSeperator(String seperator) {
		this.seperator = seperator;
	}

	public String getSeperator() {
		return seperator;
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();
		getNodeParser(NODE_PARSER_distribution).setRequired(false)
				.setUsed(true);
		addNodeParser(new UniqueNodeParser(false, true, this));
		addNodeParser(new SeperatorNodeParser(false, true, this));
		addNodeParser(new DisableRngNodeParser(false, true, this));
		getNodeParser(NODE_PARSER_file).setRequired(true);

	}

	private class DisableRngNodeParser extends Parser<DictList> {

		public DisableRngNodeParser(boolean required, boolean used,
				DictList parent) {
			super(required, used, NODE_PARSER_noRng, parent,
					"If this is true, picked lines from file correlate with table line numbers");

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
				setRngDisabled(Boolean.parseBoolean(nodeText));
			}
		}
	}

	private class UniqueNodeParser extends Parser<DictList> {

		public UniqueNodeParser(boolean required, boolean used, DictList parent) {
			super(
					required,
					used,
					NODE_PARSER_unique,
					parent,
					"Specifies if picked lines should be unique. Like taking samples from an urn without replacement");

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
				setUnique(Boolean.parseBoolean(nodeText));
			}
		}
	}

	private class SeperatorNodeParser extends Parser<DictList> {

		public SeperatorNodeParser(boolean isRequired, boolean b,
				DictList parent) {
			super(
					isRequired,
					b,
					NODE_PARSER_seperator,
					parent,
					"Sperator string inserted between multiple randomly picked lines form dict list. Standard is a single whitespace. Has only an effekt if <"
							+ NODE_PARSER_size + "> is greater 1");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String tmp = node.getTextContent();
			if (tmp != null && !tmp.isEmpty()) {
				seperator = tmp;
			} else if (this.isRequired()) {
				throw new XmlException(getNodeInfo() + "<" + this.getName()
						+ "> must not be empty.");

			}
		}
	}

}
