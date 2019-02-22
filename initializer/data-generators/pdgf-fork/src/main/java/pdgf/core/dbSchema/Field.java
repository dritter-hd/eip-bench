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
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.XmlException;
import pdgf.generator.GeneratorFactory;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Field represents a Database Table Field (or column). The Field scheme must be
 * provide via an xml subtree:
 * 
 * <br/>
 * <br/>
 * &lt;field name="fieldname1"><br/>
 * <BLOCKQUOTE> &lt;type>java.sql.Types.*&lt;/type><br/>
 * <br/>
 * &lt;primary>{true/false/empty}&lt;/primary><br/>
 * <br/>
 * &lt;unique>{true/false/empty}&lt;/unique><br/>
 * <br/>
 * &lt;length>{Integer}&lt;/length><br/>
 * <br/>
 * &lt;generator name="GeneratorClassName"><br/>
 * <BLOCKQUOTE> ...<br/>
 * </BLOCKQUOTE> &lt;/generator> </BLOCKQUOTE> &lt;/field>
 * 
 * @author Michael Frank
 * @version 1.0 13.10.2009
 */
public class Field extends Element<Generator, Table> implements Cloneable {
	// registered parsers
	public static final String NODE_PARSER_generator = "generator";
	public static final String NODE_PARSER_unique = "unique";
	public static final String NODE_PARSER_primary = "primary";
	public static final String NODE_PARSER_null = "null";
	public static final String NODE_PARSER_type = "type";
	public static final String NODE_PARSER_reference = "reference";

	public static final double NULL_CHANCE_PRECISION = Constants.NULL_CHANCE_PRECISION;

	private Node generatorConfigNode;
	private Reference[] references = null;
	private ArrayList<Reference> tempRefList = new ArrayList<Reference>();

	private String type; // as in
	private int typeAsInt; // int value of java.sql.Types
	private int nullChance = 0; // 0 for: is never null; 100 *
	// NULL_CHANCE_PRECISION for: always null
	private boolean unique = false; // is unique key
	private boolean primary = false; // is primary key

	// no loger used value cache
	AtomicInteger cacheHit = new AtomicInteger();
	AtomicInteger cacheMiss = new AtomicInteger();

	// *******************************************************************
	// constructors
	// *******************************************************************

	public Field() throws XmlException {
		this(null);

	}

	/**
	 * Creates a new empty field. Don't forget to set a Parent before calling
	 * parseConfig(Node) to initialize from a xml Subtree
	 * @throws XmlException 
	 */
	public Field(Table t) throws XmlException {
		super("Field", "Represents a Database Tables Field or Entry", t);

	}

	public void initializeReferences() throws XmlException {
		if (tempRefList.size() > 0) {
			references = new Reference[tempRefList.size()];
			for (int i = 0; i < references.length; i++) {
				references[i] = tempRefList.get(i);
				references[i].initializeReference();
			}
			tempRefList = null;

		} else {
			references = null;
			tempRefList = null;
		}

	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {

		// initialize references
		if (references != null) {
			for (int i = 0; i < references.length; i++) {
				references[i].initialize(workers);
			}
		}

		// create a Generator per Thread (worker)
		Generator[] generators = new Generator[workers];
		// backup first generator instance,created during loading of xml config
		// file
		Generator g = getChild(0);

		// re-initialize childs [] to count of workers
		setChilds(workers, g.getClass());
		try {
			addChild(g, 0);
			for (int i = 1; i < generators.length; i++) {
				g = GeneratorFactory.instance().getGenerator(
						generatorConfigNode, this);
				addChild(g, i);
			}

		} catch (InvalidElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// check null chance conditions
		if (primary && nullChance > 0) {
			throw new XmlException(getNodeInfo() + " <" + NODE_PARSER_primary
					+ "> is true but <" + NODE_PARSER_null
					+ "> is also > 0. Primary keys are not allowed to be null");
		}

		// this causes all childs to be initialized, which in this case will be
		// the workers per thread
		super.initialize(workers);
	}

	/**
	 * Get a field value for a specific row in a table. The row must be
	 * specified in {@link GenerationContext#setCurrentRow(long)}
	 * 
	 * @param
	 * @param context
	 *            context of generation like current worker, current row, etc.
	 *            Used by generators
	 * @param generatedFieldValueDTO
	 *            data transfer object in which the generated value will be
	 *            stored by the generator
	 */
	public void getFieldValueForRow(GenerationContext context,
			FieldValueDTO generatedFieldValueDTO) {
		// let workerID of calling worker start at 0 as context.getWorkerID() is
		// [1, workers]
		int workerID = context.getWorkerID() > 0 ? context.getWorkerID() - 1
				: 0;

		// get generator associated with calling worker.
		Generator g = getChild(workerID);
		AbstractPDGFRandom generatorRNG = g.getRNG(workerID);

		// field has no fixed seed or seed is not yet cached
		if (seed == null) {
			seed = this.getParent().getFieldRNGSeed(this.getElementID(),
					workerID + 1);
		}

		// set fixed or cached seed for field rng;
		elementRngs[workerID].setSeed(seed);

		// generate seed for generator rng depending on current row by
		// skipping ahead to desired row
		generatorRNG.setSeed(elementRngs[workerID].nextLong(context
				.getCurrentRow()));

		// getNextValue will store generate value in
		// generatedFieldValueDTO.value
		g.getNextValue(generatorRNG, context, generatedFieldValueDTO);

	}

	public String getType() {
		return type;
	}

	public int getTypeID() {
		return typeAsInt;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * 
	 * @param workerID
	 *            [1 , workerCount]
	 * @return
	 */
	public Generator getGenerator(int workerID) {
		return getChild(workerID - 1);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * 
	 * @return number of References
	 */
	public int referenceCount() {
		if (references == null) {
			return 0;
		}
		return references.length;
	}

	/**
	 * 
	 * @param refId
	 * @return null if no references or refId not between 0 and referenceCount()
	 */
	public Reference getReference(int refId) {
		if (references == null || refId < 0 || refId >= references.length) {
			return null;
		}
		return references[refId];
	}

	/**
	 * Returns a new FieldValue belonging to this Field. So FieldValue.getType()
	 * equals StaticHelper.getSQLType(thisField.getType())
	 * 
	 * @return FieldValue with value = null
	 */
	public FieldValueDTO getNewFieldValueDTO() {
		return new FieldValueDTO(typeAsInt, this);
	}

	@Override
	protected void configParsers() {
		getNodeParser(NODE_PARSER_size)
				.setRequired(false)
				.setUsed(true)
				.setDescription(
						"Size specifies the max \"length\" of this field.");
		getNodeParser(NODE_PARSER_rng).setRequired(false).setUsed(true);
		;
		getNodeParser(NODE_PARSER_seed).setRequired(false).setUsed(true);
		;

		addNodeParser(new GeneratorNodeParser(true, true, this));
		addNodeParser(new UniqueNodeParser(false, true, this));
		addNodeParser(new PrimaryNodeParser(false, true, this));
		addNodeParser(new NullNodeParser(false, true, this));
		addNodeParser(new TypeNodeParser(true, true, this));
		addNodeParser(new ReferenceNodeParser(false, true, this));

	}

	public void setNullChance(int nullChance) {
		this.nullChance = nullChance;
	}

	public int getNullChance() {
		return nullChance;
	}

	private class NullNodeParser extends Parser<Field> {

		public NullNodeParser(boolean required, boolean used, Field parent) {
			super(required, used, NODE_PARSER_null, parent,
					"Chance in percent from [0, 100] for this fields value to be null");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				double nullChanceDoulbe = StaticHelper.parseDoubleTextContent(
						this.getParserNodeInfo(), node, 0, 100);

				// scale nullChance to int for performance reasons.
				// Precision
				setNullChance((int) Math.round(Math.floor(nullChanceDoulbe
						* NULL_CHANCE_PRECISION)));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getParserNodeInfo());
					errMsg.append(" must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">10</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}
		}
	}

	private class GeneratorNodeParser extends Parser<Field> {

		public GeneratorNodeParser(boolean required, boolean used, Field parent) {
			super(required, used, NODE_PARSER_generator, parent,
					"Value Generator for this field");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			generatorConfigNode = node;

			// create a new generator;
			Generator g = GeneratorFactory.instance().getGenerator(node,
					this.getParent());

			// init childs
			setChilds(1, g.getClass());
			try {
				addChild(g, 0);
			} catch (InvalidElementException e) {
				throw new XmlException(getParserNodeInfo() + e.getMessage());
			}
		}

	}

	private class UniqueNodeParser extends Parser<Field> {

		public UniqueNodeParser(boolean required, boolean used, Field parent) {
			super(
					required,
					used,
					NODE_PARSER_unique,
					parent,
					"Specifies if this field should be unique {true | false}. (must be supported by the used Generator), maybe used by output plugins");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String nodeText = null;
			if (node == null || (nodeText = node.getTextContent()) == null
					|| nodeText.isEmpty()) {
				if (this.isRequired()) {
					throw new XmlException(getParserNodeInfo()
							+ " must not be empty.");
				}
			} else {
				unique = Boolean.parseBoolean(nodeText);
			}
		}
	}

	private class PrimaryNodeParser extends Parser<Field> {

		public PrimaryNodeParser(boolean required, boolean used, Field parent) {
			super(
					required,
					used,
					NODE_PARSER_primary,
					parent,
					"Specifies if this field should be primary {true | false}. (must be supported by the used Generator), maybe used by output plugins");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String nodeText = null;
			if (node == null || (nodeText = node.getTextContent()) == null
					|| nodeText.isEmpty()) {

				if (this.isRequired()) {
					throw new XmlException(getNodeInfo() + "<" + this.getName()
							+ "> must not be empty. Example: <"
							+ this.getName() + "> java.sql.Types.INTEGER</"
							+ this.getName() + "> ");
				}
			} else {
				primary = Boolean.parseBoolean(nodeText);

			}
		}
	}

	private class TypeNodeParser extends Parser<Field> {

		public TypeNodeParser(boolean required, boolean used, Field parent) {
			super(
					required,
					used,
					NODE_PARSER_type,
					parent,
					"Specifies type of this field. Type must be one of java.sql.Types.* f.e.: java.sql.Types.INTEGER. Maybe used by output plugins");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String nodeText = null;
			if (node == null || (nodeText = node.getTextContent()) == null
					|| nodeText.isEmpty()) {
				throw new XmlException(getNodeInfo() + "<" + this.getName()
						+ "> must not be empty. Example: <" + this.getName()
						+ "> java.sql.Types.INTEGER</" + this.getName() + "> ");
			} else {
				type = nodeText;
				Integer type_tmp = StaticHelper.getSQLType(type);
				if (type_tmp == null) {
					throw new XmlException(getNodeInfo() + "<" + this.getName()
							+ ">" + type + " ist not a valid SQL type");
				} else {
					typeAsInt = type_tmp.intValue();
				}
			}
		}
	}

	private class ReferenceNodeParser extends Parser<Field> {

		public ReferenceNodeParser(boolean required, boolean used, Field parent) {
			super(
					required,
					used,
					NODE_PARSER_reference,
					parent,
					"Specifies on or more references to a foreign field this field requires to be generated");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			// if is reference, type field does not matter because only refere

			Reference reference = new Reference(this.getParent());
			reference.parseConfig(node);
			tempRefList.add(reference);

		}
	}
}