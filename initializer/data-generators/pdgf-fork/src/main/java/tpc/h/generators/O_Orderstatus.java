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
import pdgf.core.dbSchema.Reference;
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
public class O_Orderstatus extends Generator {

	public O_Orderstatus() throws ConfigurationException, XmlException {
		super(
				"O_ORDERSTATUS set to the following value: \"F\" if all lineitems of this order have L_LINESTATUS set to \"F\".\"O\" if all lineitems of this order have L_LINESTATUS set to \"O\". \"P\" otherwise.");
		// TODO Auto-generated constructor stub
	}

	private static final char[] O = new char[] { 'O' };
	private static final char[] F = new char[] { 'F' };
	private static final char[] P = new char[] { 'P' };
	private SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
	private static long currentDate;

	private long rowsPrimTable;
	private Field primField;
	private int primTableId = -1;
	private Reference r = null;

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);
		try {
			currentDate = df.parse("1995-06-17").getTime();
		} catch (ParseException e) {
			throw new ConfigurationException(this.getNodeInfo()
					+ e.getMessage());
		}

		r = this.getParentField().getReference(0);
		
		
		
		if (r != null) {

			long sf = (long) this.getParentField().getParent().getParent().getScaleFactor();
			rowsPrimTable = r.getRefTable().getSize();
			if (r.getRefField() == null || r.getRefTable() == null) {
				throw new ConfigurationException(
						this.getNodeInfo()
								+ " Reference is not initialzed! can not do initializing of this generator if initializing of parents reference is done before!");

			}
			primField = r.getRefField();
			primTableId = r.getRefTable().getElementID();
		} else {
			throw new XmlException(
					this.getNodeInfo()
							+ " no reference defined for parent field! You can not use this Generator if reference is not defined.");
		}
		

	}

	@Override
	public void nextValue(AbstractPDGFRandom rng, GenerationContext gc,
			FieldValueDTO fvDTO) {
		long currRow = gc.getID();//getID == getCurRow for historical tables

		// required Lineitem rows
		long[] lineitemRows = L_Orderkey.getLineItemsForO_OrderRow(currRow);

		int[] L_LineStatus_Value_is_O = new int[lineitemRows.length];

	
		for (int i = 0; i < lineitemRows.length; i++) {
			r.getReferencedValue(lineitemRows[i], gc, fvDTO);

			

			// fvDTO.getValue() contains L_Shipdate as String
			// fvDTO.getPlainValue() contains L_Shipdate as long
			// if L_Shipdate >currentDate then value of L_LineStatus_Value_is_O
			// is '1'
			// otherwise L_LineStatus_Value_is_O = 0
			L_LineStatus_Value_is_O[i] = (Long) fvDTO.getPlainValue() > currentDate ? 1
					: 0;
		}

		boolean equals = true;
		for (int i = 0; i < L_LineStatus_Value_is_O.length - 1; i++) {
			// L_LineStatus_Value[i]>currentDate = 'O'
			equals = equals
					&& (L_LineStatus_Value_is_O[i] == L_LineStatus_Value_is_O[i + 1]);
			if (!equals) {
				fvDTO.setValue(P);
				break;
			}
		}

		if (equals && L_LineStatus_Value_is_O[0] == 0) {

			fvDTO.setValue(F);
		} else if (equals && L_LineStatus_Value_is_O[0] == 1) {
			fvDTO.setValue(O);
		}

	}

	@Override
	protected void configParsers() throws XmlException{
		super.configParsers();
		
	}

}
