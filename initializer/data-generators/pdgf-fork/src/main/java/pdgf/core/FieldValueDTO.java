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

import pdgf.core.dbSchema.Field;
import pdgf.plugin.Generator;

/**
 * Shared reusable data transfer object between Generators, Workers and Output <br/>
 * Encapsulates a Value for a {@linkplain Field}. Value generate by a
 * {@linkplain Generator} class
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class FieldValueDTO {

	private Object value = null;
	private Object plainValue = null;
	private int type;
	private Field field = null;

	/**
	 * Constructor for Field Class
	 * 
	 * @param type
	 *            java.sql.Types of value
	 * @param field
	 *            field, this Object belongs to.
	 */
	public FieldValueDTO(int type, Field field) {
		this(null, type, field);
	}

	public FieldValueDTO(Object value, int type, Field field) {
		super();
		this.value = value;
		this.type = type;
		this.field = field;
	}

	public FieldValueDTO(String value, int type) {
		super();
		this.value = value;
		this.type = type;
	}

	/**
	 * Value generated by a Generator for use in output (f.e. csv file or part
	 * of an database query).
	 * 
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Value generated by a Generator for use in output (f.e. csv file or part
	 * of an database query).
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * JDBC types as in {@linkplain java.sql.Types}
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * JDBC types as in {@linkplain java.sql.Types}
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Field for which this fieldvalue was generated for
	 * 
	 * @param field
	 */
	public void setField(Field field) {
		this.field = field;
		this.type = field.getTypeID();
	}

	/**
	 * Field for which this fieldvalue was generated for
	 */
	public Field getField() {
		return field;
	}

	/**
	 * This field saves the output of a generator similar to setValue, but
	 * plainvalue may contain the value in a more rough form. It is meant for
	 * use in a reference generator to avoid unnecessary parsing As value may
	 * contain the date "2010-12-12" as a string, plain value may contain the
	 * date represented as long: 1292108400000
	 * 
	 * @param plainValue
	 */
	public void setPlainValue(Object plainValue) {
		this.plainValue = plainValue;
	}

	/**
	 * This field saves the output of a generator similar to setValue, but
	 * plainvalue may contain the value in a more rough form. It is meant for
	 * use in a reference generator to avoid unnecessary parsing As value may
	 * contain the date "2010-12-12" as a string, plain value may contain the
	 * date represented as long: 1292108400000
	 */
	public Object getPlainValue() {

		return plainValue;
	}
}
