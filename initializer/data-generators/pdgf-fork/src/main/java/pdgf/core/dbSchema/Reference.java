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
package pdgf.core.dbSchema;

import org.w3c.dom.Node;
import pdgf.core.Element;
import pdgf.core.FieldValueDTO;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;

/**
 * Relation is a generic Class representing Relations between table fields. The
 * Relation scheme must be provide via an xml subtree:
 * 
 * <br/>
 * <br/>
 * &lt;reference><br/>
 * <BLOCKQUOTE> &lt;primTable>Tablename&lt;/primTable><br/>
 * <br/>
 * &lt;secTable>Tablename&lt;/secTable><br/>
 * <br/>
 * &lt;primField>Fieldname&lt;/primField><br/>
 * <br/>
 * &lt;secField>Fieldname&lt;/secField><br/>
 * <br/>
 * </BLOCKQUOTE> &lt;/reference>
 * 
 * @author Michael Frank
 * @version 1.0 13.10.2009
 */
public class Reference extends Element<Element, Field> {
	public static final String NODE_PARSER_referencedField = "field";
	public static final String NODE_PARSER_referencedTable = "table";
	private Table refTable;
	private String refTableName;
	private long refTableSize;
	private Field refField;
	private String refFieldName;
	private GenerationContext[] localGcs;
	private FieldValueDTO[] refFieldLocalDTO;

	public Reference(Field parent) throws XmlException {
		super("reference",
				"Represents a reference to another field in a database table",
				parent);

		this.parent = parent;
	}

	void initializeReference() throws XmlException {
		Project p = this.getParent().getParent().getParent();
		refTable = p.getChild(refTableName);

		if (refTable == null) {

			throw new XmlException(getNodeInfo()
					+ "Error while initializing References!\n"
					+ " The referenced Table: \"" + refTableName
					+ "\" that does not exist.");
		}

		refField = refTable.getChild(refFieldName);

		if (refField == null) {
			throw new XmlException(getNodeInfo()
					+ "Error while initializing References!\n"
					+ " The referenced Field: \"" + refField
					+ "\" that does not exist in referenced Table "
					+ refTableName);
		}
	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);
		Project p = this.getParent().getParent().getParent();
		localGcs = new GenerationContext[workers];
		refFieldLocalDTO = new FieldValueDTO[workers];
		for (int i = 0; i < workers; i++) {
			localGcs[i] = new GenerationContext(i + 1, workers, p);
			localGcs[i].set(refTable.getScaledTableSize(), 0,
					refTable.getScaledTableSize());
			refFieldLocalDTO[i] = refField.getNewFieldValueDTO();

		}
		refTableSize = refTable.getScaledTableSize();
	}

	public FieldValueDTO getReferencedValue(long row, GenerationContext gc) {

		getReferencedValue(row, gc, refFieldLocalDTO[gc.getWorkerID() - 1]);
		return refFieldLocalDTO[gc.getWorkerID() - 1];
	}

	public void getReferencedValue(long row, GenerationContext gc,
			FieldValueDTO fvDTO) {
		GenerationContext localGc = localGcs[gc.getWorkerID() - 1];
		localGc.setCurrentRow(row);
		refField.getFieldValueForRow(localGc, fvDTO);
	}

	/**
	 * The referenced Field, the primary key
	 * 
	 * @return the Field
	 */
	public Field getRefField() {
		return refField;
	}

	public Table getRefTable() {
		return refTable;
	}

	public void setRefTableName(String refTableName) {
		this.refTableName = refTableName;
	}

	public String getRefTableName() {
		return refTableName;
	}

	public void setRefFieldName(String primFieldName) {
		this.refFieldName = primFieldName;
	}

	public String getRefFieldName() {
		return refFieldName;
	}

	@Override
	protected void configParsers() {
		getAttrParser(ATTR_PARSER_Name).setRequired(false).setUsed(false);
		addNodeParser(new ForeignFieldNodeParser(true, true, this));
		addNodeParser(new ForeignTableNodeParser(true, true, this));
	}

	public long getRefTableSize() {
		return refTableSize;
	}

	private class ForeignTableNodeParser extends Parser<Reference> {

		public ForeignTableNodeParser(boolean required, boolean used,
				Reference parent) {
			super(required, used, NODE_PARSER_referencedTable, parent,
					"The referenced Table");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			refTableName = node.getTextContent();
		}
	}

	private class ForeignFieldNodeParser extends Parser<Reference> {

		public ForeignFieldNodeParser(boolean required, boolean used,
				Reference parent) {
			super(required, used, NODE_PARSER_referencedField, parent
					+ "The referenced Field");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			refFieldName = node.getTextContent();
		}
	}
}
