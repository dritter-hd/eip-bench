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
public class L_Commitdate extends Generator {
	private SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
	private Field f_L_Shipdate = null;

	public L_Commitdate() throws ConfigurationException , XmlException{
		super("L_COMMITDATE = O_ORDERDATE + random value [30 .. 90].");
		
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);


		String fieldName = "L_SHIPDATE";
		Field f = this.getParentField().getParent().getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName + " with a generator named: "
							+ L_Shipdate.class.getName());
		}
		f_L_Shipdate = f;
		Generator [] g = f.getChilds();
	
		if (!(g[0] instanceof L_Shipdate)) {

			throw new ConfigurationException(this.getNodeInfo() + " Field "
					+ g[0].getParent().getNodeInfo() + " must use generator \""
					+ L_Shipdate.class.getName() + "\" and " + fieldName
					+ " must be defined before " + this.getParent().getName());
		}
		
		
		// we can cheat here and directly cache last value of L_shipdate because
		// this Generator is not referenced by any other field

		
		
	}
	
	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext gc, FieldValueDTO currentFieldValue) {

	

		// calc shipping days
		long days = rng.nextLong();

		// days cannot be negative
		if (days < 0) {
			days = -days;
		}
		days = min + days % (max - min + 1);

		// convert days to days in ms
		days = days * Constants.ONE_DAY_IN_ms;

		// calc commitdate
		days =((L_Shipdate) f_L_Shipdate.getGenerator(gc.getWorkerID())).getLastOrderDate() + days;

		currentFieldValue.setPlainValue(days);

		currentFieldValue.setValue(df.format(days));

	}

	@Override
	protected void configParsers() throws XmlException{
		super.configParsers();
		
		getNodeParser(NODE_PARSER_max).setRequired(true);
		getNodeParser(NODE_PARSER_min).setRequired(true);
	}

	

}
