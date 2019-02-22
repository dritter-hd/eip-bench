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

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.dbSchema.Field;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Generator;
import pdgf.plugin.AbstractPDGFRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class L_ExtendedPrice extends Generator {

	private Field L_PARTKEY_Field;
	private Field L_QUANTITY_Field;

	public L_ExtendedPrice() throws ConfigurationException, XmlException {
		super(
				"L_EXTENDEDPRICE = l_quantity * p_retailprice where p_retailprice is from the part with P_PARTKEY = L_PARTKEY.");

	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);

		String fieldName = "L_PARTKEY";
		Field f = this.getParentField().getParent().getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName + " with a generator named: "
							+ L_Partkey.class.getName());
		} else {
			L_PARTKEY_Field = f;
		}
		fieldName = "L_QUANTITY";
		f = this.getParentField().getParent().getField(fieldName);
		if (f == null) {
			throw new XmlException(
					this.getNodeInfo()
							+ " This Generator requires the existance of a Field named: "
							+ fieldName + " with a generator named: "
							+ RandomValueXY.class.getName());
		} else {
			L_QUANTITY_Field = f;
		}

	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		// cannot use cached values here because this Generator is referenced by
		// another generator (O_Totoalprice)
		// so we need to recalculate the stuff we need.

		// get (recalculate) L_partkey for this row
		L_PARTKEY_Field.getFieldValueForRow(generationContext,
				currentFieldValue);
		long l_partkey = (Long) currentFieldValue.getValue();

		// get (recalculate) l_quantity for this row
		L_QUANTITY_Field.getFieldValueForRow(generationContext,
				currentFieldValue);
		long l_quantity = (Long) currentFieldValue.getValue();

		// calc P_retailprice from row where P_partkey = L_partkey (P_partkey =
		// rowID);
		long p_Retailprice = P_Retailprice.calcRetailpriceForPartkey(l_partkey);

		currentFieldValue.setValue(l_quantity * p_Retailprice);

	}


	
}
