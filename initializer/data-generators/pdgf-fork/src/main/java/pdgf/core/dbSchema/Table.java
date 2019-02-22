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

import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pdgf.core.Element;
import pdgf.core.Parser;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.XmlException;

import java.util.LinkedList;

/**
 * Table represents a Database Table. The Table scheme must be provide via an
 * xml subtree:
 * 
 * <br/>
 * <br/>
 * &lt;table name="ThisIsaTableName"><br/>
 * <BLOCKQUOTE> &lt;size>20000&lt;/size> //how many entrys this table should
 * have<br/>
 * <br/>
 * &lt;fields> <BLOCKQUOTE> &lt;field name="fieldname1">...&lt;/field> <br/>
 * &lt;field name="fieldname2">...&lt;/field> <br/>
 * ... </BLOCKQUOTE> &lt;/fields> </BLOCKQUOTE> &lt;/table>
 * 
 * @author Michael Frank
 * @version 1.0 13.10.2009
 */
public class Table extends Element<Field, Project> {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Table.class);
	public static final String NODE_PARSER_fields = "fields";
	public static final String NODE_PARSER_fixedSize = "fixedSize";
	public static final String NODE_PARSER_static = "static";

	public static final String NODE_PARSER_exclude = "exclude";

	private boolean fixedSize = false;
	private boolean isStatic = false;
	private boolean isExcluded = false;

	/**
	 * Creates a new Table from the given xml Subtree
	 * 
	 * @param parent
	 *            The Project this Table belongs to
	 * @throws XmlException
	 *             error while parsing the relevant part of the xml file
	 *             responsible for this table
	 */
	public Table(Project parent) throws XmlException {
		super("Table", "Represents a reference a database table", parent);

	}

	public Table() throws XmlException {
		super("Table", "Represents a reference a database table");
	}

	// *******************************************************************
	// Getter and Setters
	// *******************************************************************
	public int getFieldID(Field field) {
		return getChildID(field);
	}

	public int getFieldID(String field) {
		return getChildID(field);
	}

	public Field getField(String field) {
		return getChild(field);
	}
	
	/**
	 * 
	 * @param nameOfField
	 *            field identified by its name
	 * @param workerID
	 *            id of calling worker starting at 1
	 * @return the seed of a filed for a specific row
	 */
	public long getFieldSeed(long row, String nameOfField, int workerID) {
		return getFieldRNGSeed(getChildID(nameOfField), workerID);
	}

	/**
	 * 
	 * @param field
	 *            Filed obj identifying the field to get the seed for
	 * @param workerID
	 *            id of calling worker starting at 1
	 * @return the seed of a filed for a specific row
	 */
	public long getFieldSeed(Field field, int workerID) {
		return getFieldRNGSeed(getChildID(field), workerID);
	}

	/**
	 * 
	 * @param fieldNo
	 *            ID of field
	 * @param workerID
	 *            id of calling worker starting at 1
	 * @return the seed of a filed for a specific row
	 */
	public long getFieldRNGSeed(int fieldNo, int workerID) {
		// table seed is not fixed or seed has not been cached yet
		if (seed == null) {
			seed = this.getParent().getTableSeed(elementID, workerID);
		}

		elementRngs[workerID - 1].setSeed(seed);

		// row rng generate field seed
		return elementRngs[workerID - 1].nextLong(fieldNo + 1);

		// // table seed is not fixed or seed has not been cached yet
		// if (seed == null) {
		// seed = this.getParent().getTableSeed(elementID, workerID);
		// }
		// // use elementRngs[threadID] as Table RNG
		// elementRngs[workerID - 1].setSeed(seed);
		//
		// // use elementRngs as row rng | Table rng generate row rng Seed
		// elementRngs[workerID - 1].setSeed(elementRngs[workerID - 1]
		// .nextLong(row));
		//
		// // elementRngs[threadID] is now ROW RNG
		//
		// // row rng generate field seed
		// return elementRngs[workerID - 1].nextLong(fieldNo + 1);
	}

	/**
	 * Return scaled amount of rows for this table if (fixedSize) { <br/>
	 * return getSize();<br/>
	 * } else {<br/>
	 * return getSize() * parent.getScaleFactor();<br/>
	 * }
	 * 
	 * @return
	 */
	public long getScaledTableSize() {
		if (fixedSize) {
			return getSize();
		} else {
			return getSize() * parent.getScaleFactor();
		}
	}

	/**
	 * Table size is fixed and not influenced by Scalefactor
	 * 
	 * @param fixed
	 */
	public void setFixedSize(boolean fixed) {
		fixedSize = fixed;

	}

	/**
	 * if true : Table does not grow with increasing scaleFactor it remains at
	 * its specified size. {@linkplain Table#getScaledTableSize()} always
	 * returns the right final size as it takes the flag isFixedSize into
	 * account.
	 * 
	 * @return
	 */
	public boolean isFixedSize() {
		return fixedSize;
	}

	/**
	 * if true: Table must be generated completely on every node. Workload for
	 * this table must not be divided through nodes (but by workers!).
	 * 
	 * @param Static
	 */
	public void setStatic(boolean Static) {
		isStatic = Static;
	}

	/**
	 * if true:Table must be generated completely on every node. Workload for
	 * this table must not be divided through nodes (but by workers!).
	 * 
	 * @return
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * 
	 * If true: No values must be generated for this table. Table is only there
	 * to calculate references
	 * 
	 * @param isExcluded
	 */
	public void setExcluded(boolean isExcluded) {
		this.isExcluded = isExcluded;
	}

	/**
	 * if true: No values must be generated for this table. Table is only there
	 * to calculate references
	 * 
	 * @return
	 */
	public boolean isExcluded() {
		return isExcluded;
	}

	@Override
	protected void configParsers() {
		getNodeParser(NODE_PARSER_size)
				.setRequired(true)
				.setUsed(true)
				.setDescription(
						"Number of rows in this table at scale factor 1.");
		getNodeParser(NODE_PARSER_rng).setRequired(false).setUsed(true);
		getNodeParser(NODE_PARSER_seed).setRequired(false).setUsed(true);
		addNodeParser(new FieldsNodeParser(true, true, this));
		addNodeParser(new FixedSizeNodeParser(false, true, this));
		addNodeParser(new StaticNodeParser(false, true, this));
		addNodeParser(new ExcludeNodeParser(false, true, this));
	}

	private class FixedSizeNodeParser extends Parser<Element> {

		public FixedSizeNodeParser(boolean required, boolean used,
				Element parent) {
			super(
					required,
					used,
					NODE_PARSER_fixedSize,
					parent,
					"if true, this tables size is not influenced by project scale factor! (used scheduler must support this)");

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
				fixedSize = (Boolean.parseBoolean(nodeText));
				log.info(getNodeInfo() + "Table has fixed size: " + fixedSize);
			}
		}
	}

	private class StaticNodeParser extends Parser<Element> {

		public StaticNodeParser(boolean required, boolean used, Element parent) {
			super(
					required,
					used,
					NODE_PARSER_static,
					parent,
					"if true, this tables is generated on every node in full (scaled) size. (used scheduler must support this)");

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
				isStatic = (Boolean.parseBoolean(nodeText));
				log.info(getNodeInfo() + "Table is static on every node: "
						+ isStatic);
			}
		}
	}

	private class ExcludeNodeParser extends Parser<Element> {

		public ExcludeNodeParser(boolean required, boolean used, Element parent) {
			super(
					required,
					used,
					NODE_PARSER_exclude,
					parent,
					"if true, this tables is excluded from the generation process. It ramains as source for references. (used scheduler must support this)");

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
				isExcluded = (Boolean.parseBoolean(nodeText));
				log.info(getNodeInfo() + "Table is excluded from generation "
						+ isExcluded);
			}
		}
	}

	private class FieldsNodeParser extends Parser<Table> {

		public FieldsNodeParser(boolean required, boolean used, Table parent) {
			super(required, used, NODE_PARSER_fields, parent,
					"contains all fields of this table");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			if (!node.hasChildNodes()) {
				throw new XmlException(
						getNodeInfo()
								+ "<fields> does not have any child nodes! Please add at minimum one  <field> node");
			} else {
				NodeList fieldNodes = node.getChildNodes();
				int fieldCount = fieldNodes.getLength();

				/*
				 * // tableCount/2 Da im Domtree vor und nach jeder Element //
				 * Node eine text Node ist. setChilds(new Field[fieldCount /
				 * 2]);
				 */

				log.debug("Found " + fieldCount / 2 + " fields in table "
						+ getName());

				Node curentFieldNode = null;

				// buffer Fields into list
				LinkedList<Field> fields = new LinkedList<Field>();
				for (int j = 0; j < fieldCount; j++) {
					curentFieldNode = fieldNodes.item(j);

					if (curentFieldNode.getNodeType() == Node.ELEMENT_NODE) {
						log.debug("current field: "
								+ curentFieldNode.getNodeName());
						Field f = new Field(this.getParent());
						f.parseConfig(curentFieldNode);
						fields.add(f);

						// try {
						// addChild(new Field(this.getParent(), curentField),
						// pos);
						// } catch (InvalidElementExeption e) {
						// throw new XmlExecption(getNodeInfo() + "<"
						// + curentField.getNodeName() + "> "
						// + e.getMessage());
						// }
						// pos++;
					}
				}

				// init childs
				setChilds(fields.size(), fields.getFirst().getClass());
				// now add fields to table
				int pos = 0;
				for (Field f : fields) {

					try {
						addChild(f, pos++);
					} catch (InvalidElementException e) {
						throw new XmlException(getNodeInfo() + "<"
								+ curentFieldNode.getNodeName() + "> "
								+ e.getMessage());
					}
				}
			}
		}
	}
}