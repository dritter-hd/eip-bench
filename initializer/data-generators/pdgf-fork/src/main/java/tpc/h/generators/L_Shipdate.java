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
public class L_Shipdate extends Generator {
	private SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);

	public L_Shipdate() throws ConfigurationException, XmlException {
		super("L_SHIPDATE = O_ORDERDATE + random value [1 .. 121].");
		// TODO Auto-generated constructor stub
	}

	private long lastShipdate = 0;
	private long last_O_OrderDate = 0;

	/*
	 * // a Last value buffer per thread private ThreadLocal<Long>
	 * perThreadLastShipdate = new ThreadLocal<Long>() {
	 * 
	 * @Override protected Long initialValue() {
	 * 
	 * return new Long(0); } };
	 * 
	 * // a Last value buffer per thread private ThreadLocal<Long>
	 * perThreadLastOrderDate = new ThreadLocal<Long>() {
	 * 
	 * @Override protected Long initialValue() {
	 * 
	 * return new Long(0); } };
	 */

	private long rowsPrimTable;
	private Field primField;
	private int primTableId = 1;
	private Reference r=null;

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);
		

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
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		// calc row of orders table we need O_OrderDate from
		long currRowOrderKey = L_Orderkey
				.calcOrderRowForLineitemRow(generationContext.getID()); //getID == getCurRow for historical tables

		
		// recalc o_orderdate
		// calc an set the next value, value is set by generator of
		// referenced field
		r.getReferencedValue(currRowOrderKey,  generationContext, currentFieldValue);
		primField.getFieldValueForRow(generationContext, currentFieldValue);

		

		if (currentFieldValue.getPlainValue() != null) {
			last_O_OrderDate = (Long) currentFieldValue.getPlainValue();
		} else {
			try {

				// DEBUG!
				// System.out.println("L_Shipdate -> o_orderdate " +
				// (String)currentFieldValue.getValue());
				// get o_orderdate
				last_O_OrderDate = df.parse(
						((String) currentFieldValue.getValue())).getTime();

				// should not happen
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}

		// cheat and buffer recalculated o_oderderdate for use by L_Commitdate
		// perThreadLastOrderDate.set(date);

		// calc shipping days
		long days = rng.nextLong();

		// days cannot be negative
		if (days < 0) {
			days = -days;
		}
		//scale random day to [min, max] intervall
		days = min + days % (max - min + 1);

		// add shippingdays to o_orderdate
		lastShipdate = last_O_OrderDate + days * Constants.ONE_DAY_IN_ms;

		// cheat and buffer calculated L_Shipdate for use by L_Receiptdate
		// perThreadLastShipdate.set(date);

		currentFieldValue.setPlainValue(lastShipdate);
		currentFieldValue.setValue(df.format(lastShipdate));

	}

	public long getLastShipdate() {
		return lastShipdate;
		// return perThreadLastShipdate.get();
	}

	public Long getLastOrderDate() {
		return last_O_OrderDate;
		// return perThreadLastOrderDate.get();
	}

	@Override
	protected void configParsers() throws XmlException{
		super.configParsers();
		getNodeParser(NODE_PARSER_min).setRequired(true).setUsed(true);
		getNodeParser(NODE_PARSER_max).setRequired(true).setUsed(true);

	}

}
