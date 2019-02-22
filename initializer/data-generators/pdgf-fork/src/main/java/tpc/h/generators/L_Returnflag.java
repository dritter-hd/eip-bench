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
package tpc.h.generators;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.dbSchema.Field;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.plugin.AbstractPDGFRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class L_Returnflag extends Generator {

	private static final char[] R = new char[] { 'R' };
	private static final char[] A = new char[] { 'A' };
	private static final char[] N = new char[] { 'N' };
	private static long currentDate;

	private SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);

	private Field f_l_receiptDateGenerator;

	public L_Returnflag() throws ConfigurationException, XmlException {
		super(
				"L_RETURNFLAG set to a value selected as follows: If L_RECEIPTDATE <= CURRENTDATE then either \"R\" or \"A\" is selected at random else \"N\" is selected.");
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);
		

		try {
			currentDate = df.parse("1995-06-17").getTime();
		} catch (ParseException e) {
			throw new ConfigurationException(this.getNodeInfo()
					+ e.getMessage());
		}

		String fieldName = "L_RECEIPTDATE";
		Field f = this.getParentField().getParent().getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName + " with a generator named: "
							+ L_Shipdate.class.getName());
		}
		f_l_receiptDateGenerator = f;
		Generator g = this.getParentField().getParent().getField(fieldName)
				.getGenerator(1);
		if (!(g instanceof L_Receiptdate)) {

			throw new ConfigurationException(this.getNodeInfo() + " Field "
					+ g.getParent().getNodeInfo() + " must use generator \""
					+ L_Shipdate.class.getName() + "\" and " + fieldName
					+ " must be defined before " + this.getParent().getName());
		}
		
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext gc, FieldValueDTO currentFieldValue) {

		

		if (((L_Receiptdate) f_l_receiptDateGenerator.getGenerator(gc.getWorkerID())).getLastReceiptDate() <= currentDate) {
			if (rng.nextBoolean()) {
				currentFieldValue.setValue(R);
			} else {
				currentFieldValue.setValue(A);
			}
		} else {
			currentFieldValue.setValue(N);
		}
	}
	@Override
	protected void configParsers() throws XmlException{
		super.configParsers();
		
	}

}
