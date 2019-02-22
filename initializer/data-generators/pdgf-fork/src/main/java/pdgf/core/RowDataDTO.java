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
package pdgf.core;

import pdgf.core.dbSchema.Table;

/**
 * Data transfer object for a generated Row.
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class RowDataDTO {

	public Table parent = null;
	public FieldValueDTO[] fieldValues;
	public long row = 0;

	// *******************************************************************
	// constructors
	// *******************************************************************
	public RowDataDTO() {

	}

	public RowDataDTO(int size) {
		fieldValues = new FieldValueDTO[size];
	}

	public RowDataDTO(Table parent) {
		super();
		this.parent = parent;

	}

	public RowDataDTO(Table parent, FieldValueDTO[] fieldList) {
		this.parent = parent;
		this.fieldValues = fieldList;
	}

	// *******************************************************************
	// getters and setters
	// *******************************************************************

	/**
	 * Table this RowData belongs to
	 */
	public Table getParent() {
		return parent;
	}

	public void setParent(Table parent) {
		this.parent = parent;
	}

	public FieldValueDTO[] getFieldValuesList() {
		return fieldValues;
	}

	public void reset(Table newParent) {
		this.parent = newParent;
		this.fieldValues = new FieldValueDTO[parent.getChildsCount()];
		for (int i = 0; i < fieldValues.length; i++) {
			fieldValues[i] = parent.getChild(i).getNewFieldValueDTO();
		}

	}

	/**
	 * Gets the FieldValue of the Field with the given name
	 * 
	 * @param name
	 *            name of the field to return
	 * @return
	 */
	public FieldValueDTO getFieldValue(String name) {
		for (FieldValueDTO f : fieldValues) {
			if (f.getField().getName().equals(name)) {
				return f;
			}
		}
		return null;
	}

	public void setRow(long currentRowID) {
		this.row = currentRowID;

	}

	public long getRow() {
		return row;
	}

}
