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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;

import pdgf.core.FieldValueDTO;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.util.random.PdgfDefaultRandom;

/**
 * Generates date between startdate and enddate
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class DateGenerator extends Generator {
	public static final String NODE_PARSER_start = "startDate";
	public static final String NODE_PARSER_end = "endDate";

	private SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);

	private long startDate = 0l;
	private long endDate = 0l;
	private int daysBetween;

	public DateGenerator() throws XmlException {
		super(
				"Generates a random Date beween startdate and enddate, supports different distributions");
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws XmlException {
		DateGenerator dg = new DateGenerator();
		dg.setStartDate("1992-01-01");
		dg.setEndeDate("1998-12-31");
		Date d = null;
		try {
			d = dg.df.parse("1998-12-31");
			System.out.println("2010-12-12 "
					+ dg.df.parse("2010-12-12").getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("1998-12-31  minus 151 tage: "
				+ dg.df.format(d.getTime() - 151 * Constants.ONE_DAY_IN_ms));

		System.out.println("startdate: " + dg.df.format(dg.getStartDate()));
		System.out.println("endtdate: " + dg.df.format(dg.getEndDate()));
		System.out.println("daysBeteween: " + dg.getDaysBetweenStartEnd());

		FieldValueDTO fwdto = new FieldValueDTO(java.sql.Types.DATE, null);
		PdgfDefaultRandom rng = new PdgfDefaultRandom();
		GenerationContext gc = new GenerationContext();
		for (int i = 0; i < 15; i++) {
			dg.nextValue(rng, gc, fwdto);
			System.out.println(fwdto.getValue());
		}
		System.out.println("start benchmarking...");

		// Benchmark
		long start = System.currentTimeMillis();
		long iterations = 10000000;
		for (int i = 0; i < iterations; i++) {
			dg.nextValue(rng, gc, fwdto);

		}

		long time = System.currentTimeMillis() - start;
		System.out.println(iterations + " iterations in " + time + "ms "
				+ ((iterations * 1000) / time) + " dates/s");

		// Test min max value
		// while (true) {
		//
		// dg.getNextValue(rng, gc, fwdto);
		// if(fwdto.getValue().equals("1992-01-01")||
		// fwdto.getValue().equals("1998-12-31")){
		// System.out.println(fwdto.getValue());
		// }
		// }

	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {
		// TODO Auto-generated method stub
		// Calendar cal = Calendar.getInstance();
		int rand;
		if (this.getDistribution() == null) {
			rand = rng.nextInt(daysBetween + 1);
		} else {
			rand = this.getDistribution().nextIntValue(rng, generationContext)
					% (daysBetween + 1);
		}

		/*
		 * long rand = rng.nextLong(); if(rand <0){ rand = -rand; } rand = rand
		 * %(daysBetween + 1);
		 */

		// currentFieldValue.setValue(startDate + (rand * ONE_DAY));

		// set plain for references
		currentFieldValue.setPlainValue(startDate
				+ (rand * Constants.ONE_DAY_IN_ms));

		// set for output
		currentFieldValue.setValue(df.format(startDate
				+ (rand * Constants.ONE_DAY_IN_ms)));
	}

	public int getDaysBetweenStartEnd() {
		return daysBetween;
	}

	public long getStartDate() {
		return startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndeDate(String nodeText) throws XmlException {
		try {
			endDate = parseDate(nodeText);
			calcDaysBetween();
		} catch (XmlException e) {
			throw new XmlException(getNodeInfo() + "<" + this.getName() + "> "
					+ e.getMessage());
		}
	}

	public void setStartDate(String nodeText) throws XmlException {
		try {
			startDate = parseDate(nodeText);
			calcDaysBetween();

		} catch (XmlException e) {
			throw new XmlException(getNodeInfo() + "<" + this.getName() + "> "
					+ e.getMessage());
		}
	}

	private long parseDate(String nodeText) throws XmlException {
		try {
			return df.parse(nodeText).getTime();

		} catch (ParseException e) {
			throw new XmlException(getNodeInfo() + "<" + this.getName()
					+ "> does not contain an acceptable date.");
		}
	}

	private void calcDaysBetween() throws XmlException {
		if (startDate != 0 && endDate != 0) {
			long days = (endDate - startDate) / Constants.ONE_DAY_IN_ms;
			if (days <= Integer.MAX_VALUE) {
				daysBetween = (int) days;
			} else {
				throw new XmlException(
						"To much time between Startdate and enddate. Max "
								+ Integer.MAX_VALUE + " days allowed");
			}
		}
	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();
		getNodeParser(NODE_PARSER_distribution).setRequired(false)
				.setUsed(true);
		addNodeParser(new EndNodeParser(true, true, this));
		addNodeParser(new StartNodeParser(true, true, this));
	}

	private class StartNodeParser extends Parser<Generator> {

		public StartNodeParser(boolean required, boolean used, Generator parent) {
			super(required, used, NODE_PARSER_start, parent,
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
				setStartDate(nodeText);
			}
		}

	}

	private class EndNodeParser extends Parser<Generator> {

		public EndNodeParser(boolean required, boolean used, Generator parent) {
			super(required, used, NODE_PARSER_end, parent, "Last Date");

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
				setEndeDate(nodeText);
			}
		}

	}
}
