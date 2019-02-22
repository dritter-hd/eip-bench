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

import java.text.DecimalFormat;

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.dbSchema.Field;
import pdgf.core.dbSchema.Project;
import pdgf.core.dbSchema.Table;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;
import pdgf.plugin.AbstractPDGFRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class O_TotalPrice extends Generator {
	private long rowsLineItemTable;
	private Field L_ExtendedPrice_Field;
	private Field L_Tax_Field;
	private Field L_Discount_Field;

	private int L_Tax_Field_DECIMAL_PLACES;
	private int L_Discount_Field_DECIMAL_PLACES;
	private int DECIMAL_PLACES = Constants.INT_NOT_SET;
	private int DECIMAL_PLACES_SCALE = Constants.INT_NOT_SET;
	private Project p = null;


	public O_TotalPrice() throws ConfigurationException, XmlException {
		super(
				"O_TOTALPRICE computed as: sum (L_EXTENDEDPRICE * (1+L_TAX) * (1-L_DISCOUNT)) for all LINEITEM of this order.");
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);

		String tableName = "LINEITEM";
		p = getParentField().getParent().getParent();
		Table lineitem = p.getChild(tableName);

		

		if (lineitem == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Table named: "
							+ tableName);
		}
		rowsLineItemTable = lineitem.getSize();
		
		String fieldName = "L_EXTENDEDPRICE";
		Field f = lineitem.getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName);
		} else {
			L_ExtendedPrice_Field = f;
		}
		fieldName = "L_TAX";
		f = lineitem.getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName);
		} else {
			L_Tax_Field = f;
		}

		fieldName = "L_DISCOUNT";
		f = lineitem.getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName);
		} else {
			L_Discount_Field = f;
		}

		
	}

	@Override
	public void nextValue(AbstractPDGFRandom rng, GenerationContext gc,
			FieldValueDTO fvDTO) {
		if (DECIMAL_PLACES == Constants.INT_NOT_SET) {

			// get number of decimal places from generator
			L_Tax_Field_DECIMAL_PLACES = ((RandomValueXY) L_Tax_Field.getGenerator(gc.getWorkerID())).getDecimalPlaces();
			L_Discount_Field_DECIMAL_PLACES =  ((RandomValueXY) L_Discount_Field.getGenerator(gc.getWorkerID())).getDecimalPlaces();

			// calc max decimal places
			DECIMAL_PLACES = Math.max(L_Discount_Field_DECIMAL_PLACES,
					L_Tax_Field_DECIMAL_PLACES);

			// set scale multiplier to max
			L_Discount_Field_DECIMAL_PLACES = StaticHelper.pow(10,
					(DECIMAL_PLACES - L_Discount_Field_DECIMAL_PLACES));

			L_Tax_Field_DECIMAL_PLACES = StaticHelper.pow(10,
					(DECIMAL_PLACES - L_Tax_Field_DECIMAL_PLACES));

			DECIMAL_PLACES_SCALE = StaticHelper.pow(10, DECIMAL_PLACES);
		}

		// backup gc values
		long currRow = gc.getCurrentRow();
		long id = gc.getID();
		long rows = gc.getRows();
		long rowstart = gc.getRowStart();
		long rowstop = gc.getRowStop();

		// required Lineitem rows
		long[] lineitemRows = L_Orderkey.getLineItemsForO_OrderRow(currRow);

		long sum = 0;
		long L_ExtendedPrice_Value;

		double L_Tax, L_Discount;

		gc.set(rowsLineItemTable, 0, rowsLineItemTable);
		
		for (int i = 0; i < lineitemRows.length; i++) {

			gc.setCurrentRow(lineitemRows[i]);
			gc.setID(lineitemRows[i]);
			
			// calc reference
			L_ExtendedPrice_Field.getFieldValueForRow(gc, fvDTO);
			L_ExtendedPrice_Value = (Long) fvDTO.getValue()
					* DECIMAL_PLACES_SCALE;
			// calc reference
			L_Discount_Field.getFieldValueForRow(gc, fvDTO);
			L_Discount = ((Long) fvDTO.getPlainValue())
					* L_Discount_Field_DECIMAL_PLACES;

			// calc reference
			L_Tax_Field.getFieldValueForRow(gc, fvDTO);
			L_Tax = ((Long) fvDTO.getPlainValue()) * L_Tax_Field_DECIMAL_PLACES;
			sum += (L_ExtendedPrice_Value * (((1 * DECIMAL_PLACES_SCALE) + L_Tax) * ((1 * DECIMAL_PLACES_SCALE) - L_Discount)))
					/ (DECIMAL_PLACES_SCALE * DECIMAL_PLACES_SCALE);

		}

		// revert gc
		gc.set(currRow, rows, rowstart, rowstop, p);
		gc.setID(id);
		fvDTO.setValue(StaticHelper.longToNumberWithDecimalPlaces(sum,
				DECIMAL_PLACES));

	}

	@Override
	protected void configParsers() throws XmlException {
		super.configParsers();

	}

}
