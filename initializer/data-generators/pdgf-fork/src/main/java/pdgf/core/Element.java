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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Generator;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;
import pdgf.util.random.RandomFactory;

/**
 * Superclass of all dbSchema elements and Generator/Distribution -plugins <br/>
 * Composit pattern.
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 * @param <Child>
 *            Child Elements of this element in an element tree
 * @param <Parent>
 *            Paraent Element of this element in an element tree
 */
public abstract class Element<Child extends Element, Parent extends Element> {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Element.class);
	// Names of default node and attribute parsers
	public static final String NODE_PARSER_max = "max";
	public static final String NODE_PARSER_min = "min";
	public static final String NODE_PARSER_size = "size";
	public static final String NODE_PARSER_seed = "seed";
	public static final String NODE_PARSER_rng = "rng";
	public static final String ATTR_PARSER_Name = "name";

	// parsers
	private List<Parser> attrParsers = null;
	private List<Parser> nodeParsers = null;

	private String description = null;

	private static final String ROOT = "!ROOT";

	protected String nodeTag = null; // XML Tagname of this node

	protected String name = null; // name attribute of this element

	// contains information about where this element is located in the xml
	// config tree
	private String nodeInfo = null;

	// id of this Elmement equals this.getParent().getChildId(this);
	protected int elementID = 0;

	// Parent element of this element
	protected Parent parent = null;

	// child elements of this element
	private Child[] childs = null;
	private Map<String, Integer> childNames = null;

	// Default parsed fields aviable for all elements
	// classname of this elements random number generator
	protected long size = Constants.LONG_NOT_SET;
	protected long min = Constants.LONG_NOT_SET;
	protected long max = Constants.LONG_NOT_SET;
	protected Long seed = null;
	protected String rngName = Constants.DEFAULT; // default rng

	// a AbstractPDGFRandom per thread
	protected AbstractPDGFRandom[] elementRngs = null;

	/**
	 * Creates a new Element. After initializing,
	 * {@link Element#parseConfig(Node)} is NOT!! automatically called
	 * 
	 * @param nodeTagName
	 *            the NodeName the node of this Element.If node is &lt;foo>
	 *            nodeTagName must be foo
	 * @param parent
	 *            the parent element of this element
	 * @throws XmlException 
	 */
	public Element(String nodeTagName, String description) throws XmlException {
		this(nodeTagName, description, null);

	}

	/**
	 * Creates a new Element. After initializing,
	 * {@link Element#parseConfig(Node)} is NOT!! automatically called
	 * 
	 * @param nodeTagName
	 *            the NodeName the node of this Element. If node is &lt;foo>
	 *            nodeTagName must be foo
	 * @param parent
	 *            the parent element of this element
	 * @throws XmlException 
	 */
	public Element(String nodeTagName, String description, Parent parent) throws XmlException {
		super();

		this.parent = parent;
		this.nodeTag = nodeTagName;
		this.nodeInfo = "<" + nodeTagName + ">";
		this.attrParsers = new LinkedList<Parser>();
		this.nodeParsers = new LinkedList<Parser>();
		this.childNames = new HashMap<String, Integer>();
		this.setDescription(description != null ? description : "");
		addAttrParser(new NameAttrParser(true));
		addNodeParser(new SeedNodeParser(false));
		addNodeParser(new RNGNodeParser(false, this));
		addNodeParser(new SizeNodeParser(false));
		addNodeParser(new MinNodeParser(false));
		addNodeParser(new MaxNodeParser(false));
		configParsers();

	}

	/**
	 * Parses the given node if node != null;
	 * 
	 * @param node
	 * @throws XmlException
	 */
	public void parseConfig(Node node) throws XmlException {
		if (node == null) {
			log.debug(getNodeInfo() + " NODE WAS NULL!");
		} else {

			// check if this element is responsible for parsing this node
			if (!node.getNodeName().equalsIgnoreCase(nodeTag)) {
				throw new XmlException("<" + nodeTag
						+ "> tag was exected instead of: <"
						+ node.getNodeName() + ">");
			}

			// Parse Attributes of this node
			NamedNodeMap nnm = node.getAttributes();
			int attrCount = nnm.getLength();
			boolean foundParser;
			Node attr;
			for (int i = 0; i < attrCount; i++) {
				foundParser = false;
				attr = nnm.item(i);

				if (!attr.getNodeName().equals(Constants.TEXT_NODE)
						&& !attr.getNodeName().equals(Constants.COMMENT_NODE)) {
					// find a suitable parser

					for (Parser ap : attrParsers) {
						if (ap.getName().equalsIgnoreCase(attr.getNodeName())) {
							ap.parseNode(attr);
							foundParser = true;
							break;
						}
					}
					if (!foundParser) {
						throw new XmlException(
								getNodeInfo()
										+ "<"
										+ nodeTag
										+ " "
										+ attr.getNodeName()
										+ "=...>  This attribute is unknown. Please delete this attribute.");
					}
				}
			}

			// check if all required attributes are present
			StringBuilder missing = new StringBuilder();
			for (Parser ap : attrParsers) {

				if (ap.isRequired() && !ap.isExecuted()) {
					missing.append(ap.getName());
					missing.append(", ");
				}
			}
			if (missing.length() > 0) {
				missing.insert(0, getNodeInfo()
						+ " is missing the Following node Attributes: ");
				throw new XmlException(missing.toString());
			}

			// Parse Childs of this node
			if (node.hasChildNodes()) {

				NodeList childNodes = node.getChildNodes();

				// // after every element node comes on #text node -> /2
				// int childCount = childNodes.getLength() / 2;
				// childs = (Child[]) new Child[childCount];

				Node aChild;
				String aChildName;

				// find a suitable parser
				for (int j = 0; j < childNodes.getLength(); j++) {
					foundParser = false;
					aChild = childNodes.item(j);
					aChildName = aChild.getNodeName();

					if (!aChildName.equals(Constants.TEXT_NODE)
							&& !aChildName.equals(Constants.COMMENT_NODE)) {

						for (Parser np : nodeParsers) {

							if (aChildName.equalsIgnoreCase(np.getName())) {

								np.parseNode(aChild);

								foundParser = true;
								break;
							}
						}
						if (!foundParser) {
							throw new XmlException(getNodeInfo() + "<"
									+ aChildName
									+ "> is unknown. Please delete this node.");
						}
					}
				}
			}

			// check if all required attributes are present
			missing = new StringBuilder();
			for (Parser np : nodeParsers) {

				if (np.isRequired() && !np.isExecuted()) {

					missing.append('<');
					missing.append(np.getName());
					missing.append(">, ");
				}
			}
			if (missing.length() > 0) {
				missing.insert(0, getNodeInfo()
						+ " is missing the following required Child Nodes: ");
				throw new XmlException(missing.toString());
			}

		}

		setRNGName();

	}

	/**
	 * IMPORTANT: if you override this method be sure to call
	 * super.initialize(int workers) before!!! doing your own configuration
	 * stuff or the random number generators will not be available for use.<br/>
	 * <br/>
	 * In this method you can do some configuration stuff shortly before the
	 * first call to the other methods is invoked. This Method is called after
	 * import of configuration files is finished and before before the
	 * generation process begins. More formally this method is called by
	 * {@link Generator#initialize()} before starting the worker thread(s). <br/>
	 * <br/>
	 * Initializes a seperate Random number gernerator for each Worker (Thread)
	 * 
	 * @param workers
	 *            number of worksers
	 * @throws ConfigurationException
	 */

	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		elementRngs = new AbstractPDGFRandom[workers];

		// init rngs of this elements
		for (int i = 0; i < elementRngs.length; i++) {
			elementRngs[i] = getNewElementRng(System.currentTimeMillis());

		}
		// init rngs of childs
		if (childs != null && childs.length > 0) {
			for (int i = 0; i < childs.length; i++) {
				childs[i].initialize(workers);
			}
		}

	}

	/**
	 * if this element has no RNG node set, set rngName field to name of
	 * standard RNG (Root node MUST have a RNG node)
	 * 
	 * @throws XmlException
	 */
	private void setRNGName() throws XmlException {
		// RNG not specified, use RNG of ROOT element
		if (rngName == null || rngName.equals(Constants.DEFAULT)
				|| rngName.equals("")) {
			Element e = this;
			while (e.getParent() != null) {
				e = e.getParent();
			}
			// now this must be the root element
			this.rngName = e.getRngName();
			if (this.rngName == null) {
				throw new XmlException(
						getNodeInfo()
								+ "The root element in the config file MUST contain a <rng> node specifing the default rng node to use if no other choice is made");
			}
		}
	}

	public String getNodeTagName() {
		return nodeTag;
	}

	/**
	 * Returns the child of this elements childs.
	 * 
	 * @return
	 */
	public Child getChild(int i) {
		return childs[i];
	}

	/**
	 * Returns the firstChild of this elements childs.
	 * 
	 * @return
	 */
	public Child getChild() {
		return childs[0];
	}

	/**
	 * Array of this elements childs
	 * 
	 * @return the childs [] of this element
	 */
	public Child[] getChilds() {
		return childs;
	}

	/**
	 * number of childs or Length of childs array
	 * 
	 * @return Length of childs array
	 */
	public int getChildsCount() {
		return childs.length;
	}

	/**
	 * child for Child name = element.getName()
	 * 
	 * @param name
	 *            equals childElement.getName()
	 * @return child element or null if child does not exist
	 */
	public Child getChild(String name) {
		Integer i = childNames.get(name);
		return i == null ? null : childs[i];
	}

	/**
	 * replace this Elements Array of childs with the provided one.
	 * 
	 * @param childs
	 *            the childs to set
	 */
	public void setChilds(int childCount, Class child) {
		// this.childs = (Child[]) new Object[childCount];

		// this.childs=(Child[])Array.newInstance(a,childCount);

		this.childs = (Child[]) Array.newInstance(child, childCount);

		this.childNames = new HashMap<String, Integer>();
	}

	/**
	 * Adds a child at position i to the array of childs. i must not be grater
	 * then getChildsCount()
	 * 
	 * @param child
	 *            the child to be added
	 * @param i
	 *            the position in childs[]
	 * @return the previously associated child at childs[i]
	 * @throws InvalidElementException
	 */
	public boolean addChild(Child child, int i) throws InvalidElementException {

		boolean replaced = false;
		if (i > childs.length || i < 0) {
			throw new InvalidElementException(
					getNodeInfo()
							+ " Could not add Child because Index of child is out of bounds. Size: "
							+ childs.length + " index: " + i);
		} else if (child.getName() == null || child.getName().isEmpty()) {
			throw new InvalidElementException(
					getNodeInfo()
							+ " Could not add Child because: Child name was null or empty");
		} else {
			this.childs[i] = child;
			child.setElementID(i);
			replaced = this.childNames.put(child.getName(), i) != null;
		}
		return replaced;
	}

	/**
	 * id of element equals i in childs[i]
	 * 
	 * @param name
	 *            Name of element to return id for
	 * @return id of element equals i in childs[i]
	 */
	public int getChildID(String name) {
		return childNames.get(name);
	}

	/**
	 * id of element equals i in childs[i]
	 * 
	 * @param name
	 *            Name of element to return id for
	 * @return id of element equals i in childs[i]
	 */
	public int getChildID(Element child) {
		return childNames.get(child.getName());
	}

	// /**
	// * gets Seed Of Element
	// *
	// * @param elementNumber
	// * @return Seed Of Element
	// */
	// public long getChildSeed(int elementNumber) {
	// // TODO!
	// rng.setSeed(this.getSeed());
	// // rng.skip(elementNumber)
	// return rng.nextLong(elementNumber);
	// }
	//
	// /**
	// * gets Seed Of Element
	// *
	// * @param elementNumber
	// * @return Seed Of Element
	// */
	// public long getChildSeed(String elementName) {
	// return getChildSeed(childNames.get(elementName));
	// }

	/**
	 * Map of all Names of this elements childs
	 * 
	 * @return the childNames
	 */
	public Map<String, Integer> getChildNames() {
		return new HashMap<String, Integer>(childNames);
	}

	/**
	 * Name of this Element
	 * 
	 * @return the name, or null if not set.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Name of this Element
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Default parsed node &lt;size> node. Use or meaning of "size" depends on
	 * specific element.
	 * <p>
	 * f.e. if this element is a database table, size could mean the number of
	 * rows in this table
	 * 
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Default parsed node &lt;size> node. Use or meaning of "size" depends on
	 * specific element.
	 * <p>
	 * f.e. if this element is a database table, size could mean the number of
	 * rows in this table
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Default parsed node &lt;seed> node. If this element should use a fixed
	 * seed.
	 * 
	 * @return the seed
	 */
	public Long getSeed() {
		return seed;
	}

	/**
	 * Default parsed node &lt;seed> node. If this element should use a fixed
	 * seed.
	 * 
	 * @param seed
	 *            the seed to set
	 */
	public void setSeed(Long seed) {
		this.seed = seed;
	}

	/**
	 * Info where this element is located in the Nodes Tree.
	 * 
	 * @return the nodeInfo
	 */
	public String getNodeInfo() {
		if (parent == null || parent.name == null || parent.name.equals(ROOT)) {
			return nodeInfo;
		} else {
			return parent.getNodeInfo() + this.nodeInfo;

		}
	}

	/**
	 * the elementID of this element
	 * <p>
	 * id of this Elmement equals this.getParent().getChildId(this);
	 * 
	 * @return the elementID of this element
	 */
	public int getElementID() {
		return elementID;
	}

	/**
	 * the elementID of this element to set
	 * <p>
	 * id of this Elmement equals this.getParent().getChildId(this);
	 * 
	 * @param elementID
	 *            the elementID of this element to set
	 */
	public void setElementID(int elementID) {
		this.elementID = elementID;
	}

	/**
	 * the parent element of this element
	 * 
	 * @return the parent element
	 */
	public Parent getParent() {
		return parent;
	}

	/**
	 * the parent element of this element
	 * 
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Parent parent) {
		this.parent = parent;
	}

	public void setRngName(String rngName) {
		this.rngName = rngName;
	}

	/**
	 * Returns the class name of the RNG class this Element should use. If the
	 * class name is not set or "default" then the RNG of the Root element in
	 * the element tree is returned.
	 * 
	 * @return classname of this elements RNG class
	 */
	public String getRngName() {

		return rngName;
	}

	public void setMin(long min) {
		this.min = min;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;

	}

	/**
	 * Returns Rng class for this element. Class is instance of
	 * this.getRngName(). If this.getRngName() ==null or Constants.DEFAULT then
	 * the rng classname of the root element is used. only available if this
	 * element is initialized.
	 * 
	 * @param threadId
	 *            the id of the calling thread/worker
	 * @return instance of AbstractPDGFRandom associated to calling threadID
	 */
	public AbstractPDGFRandom getRNG(int threadId) {
		return elementRngs[threadId];
	}

	public void setDescription(String description) {
		this.description = description;
		if (description == null || description == "") {
			throw new RuntimeException(
					"Description of Plugin is empty! You must provide a maningfull description to your plugin!");
		}
	}

	public String getDescription() {
		return description;

	}

	/**
	 * Returns a new Rng class for this element. Class is instance of
	 * this.getRngName(). If this.getRngName() ==null or Constants.DEFAULT then
	 * the rng classname of the root element is used.
	 * 
	 * @param seed
	 *            the seed the new rng should be set to
	 * @return a new instance of AbstractPDGFRandom
	 */
	public AbstractPDGFRandom getNewElementRng(Long seed) {
		return getNewElementRng(this.getRngName(), seed);
	}

	/**
	 * Returns a new Rng class for this element. Class is instance of className.
	 * 
	 * @param className
	 *            new Class is instance of className.
	 * @param seed
	 *            the seed the new rng should be set to
	 * @return a new instance of AbstractPDGFRandom
	 */
	public static AbstractPDGFRandom getNewElementRng(String className,
			Long seed) {
		AbstractPDGFRandom rng = null;
		try {
			rng = RandomFactory.instance().getRNGClass(className);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			// TODO!
		}
		if (seed != null) {
			rng.setSeed(seed);
		}
		return rng;

	}

	/**
	 * Returns a new Rng class for the provided element e. Class is instance of
	 * e.getRngName(). If e.getRngName() ==null or PDGFConstants.DEFAULT then
	 * the rng classname of the root element is used.
	 * 
	 * @param e
	 *            the element to get the rng for
	 * @param seed
	 *            the seed the new rng should be set to
	 * @return a new instance of AbstractPDGFRandom
	 */
	public static AbstractPDGFRandom getNewElementRng(Element e, Long seed) {
		return getNewElementRng(e.getRngName(), seed);
	}

	/**
	 * Adds a Attribute Parser Object to this element A Attribute Parser Object
	 * is Responsible for parsing Attribute in a node like the "name" attribute
	 * &lt;Element name=".."> <br/>
	 * Attributeparsers are called automatically by
	 * {@link Element#parseConfig(Node)}
	 * 
	 * @param attrParser
	 *            the attrParser to add
	 */
	public void addAttrParser(Parser attrParser) {
		attrParser.setIsAttrParser(true);
		this.attrParsers.add(attrParser);
	}

	/**
	 * Returns the parser with the specified name. Or more formal: returns the
	 * parser in the list where parserInList.getName().equalsIgnoreCase(name)<br/>
	 * Note: name should be the nodeName. <br/>
	 * if Node is &lt;foo> name should be foo too
	 * 
	 * @param name
	 *            name of the parser to search
	 * @return parser or null if this parser is not in the list
	 */
	public Parser getAttrParser(String name) {
		for (Parser ap : attrParsers) {
			if (ap.getName().equalsIgnoreCase(name)) {
				return ap;
			}
		}
		return null;
	}

	public List<Parser> getAttrParserList() {
		return attrParsers;
	}

	public List<Parser> getNodeParserList() {
		return nodeParsers;
	}

	/**
	 * Returns the parser with the specified name. Or more formal: returns the
	 * parser in the list where parserInList.getName().equalsIgnoreCase(name)
	 * 
	 * @param name
	 *            name of the parser to search
	 * @return nodeparser or null if this parser is not in the list
	 */
	public Parser getNodeParser(String name) {
		for (Parser np : nodeParsers) {
			if (np.getName().equalsIgnoreCase(name)) {
				return np;
			}
		}
		return null;
	}

	/**
	 * /** Adds a Node Parser Object to this element A Node Parser Object is
	 * Responsible for parsing a Child Node of this element<br/>
	 * &lt;Element name=".."> <br/>
	 * <blockquote> &lt;childNode>&lt;/childNode><br/>
	 * &lt;childNode>&lt;/childNode> </blockquote><br/>
	 * 
	 * &lt;/Element> Node Parsers are called automatically by
	 * {@link Element#parseConfig(Node)}
	 * 
	 * @param nodeParser
	 *            the nodeParser to add
	 */
	public void addNodeParser(Parser nodeParser) {
		nodeParser.setIsAttrParser(false);
		this.nodeParsers.add(nodeParser);
	}

	/**
	 * add here your own parsers to the Parsers lists<br />
	 * This method is automatically called when a new subclass of Element is
	 * created.
	 * 
	 * <p>
	 * More formally, it is called as last line in the Element() constructor.
	 * 
	 * <p>
	 * to add a new Node Attribute Parser use: <br />
	 * addAttrParser(new Parser());
	 * <p>
	 * to add a new Node Parser use:<br />
	 * addNodeParser(new Parser());
	 * <p>
	 * to edit a Parser (of superclass) use <br />
	 * getNodeParser(parserName) or <br />
	 * getAttrParser(parserName)<br />
	 * @throws XmlException 
	 * 
	 */
	protected abstract void configParsers() throws XmlException;

	/*
	 * public Element<Child, Parent> deepCLone( ) throws IOException,
	 * ClassNotFoundException { ByteArrayOutputStream baos = new
	 * ByteArrayOutputStream(); new ObjectOutputStream( baos ).writeObject( this
	 * ); ByteArrayInputStream bais = new ByteArrayInputStream(
	 * baos.toByteArray() ); return (Element<Child, Parent> )new
	 * ObjectInputStream(bais).readObject(); }
	 */
	private class NameAttrParser extends Parser {

		public NameAttrParser(boolean required) {
			super(
					required,
					true,
					ATTR_PARSER_Name,
					"DefaultParser. (Class)Name of this element. Used to identify plugin Class. Full name is required. Example: com.en.myPluginPackage.myPuginClass");
		}

		public NameAttrParser() {
			super(
					true,
					true,
					ATTR_PARSER_Name,
					"DefaultParser. (Class)Name of this element. Used to identify plugin Class. Full name is required. Example: com.en.myPluginPackage.myPuginClass");
		}

		@Override
		public void parse(Node attr) throws XmlException {

			String text = attr.getTextContent();

			if (text != null && !text.isEmpty()) {
				name = text;
				nodeInfo = "<" + nodeTag + " " + this.getName() + "=" + name
						+ ">";
			} else {
				if (isRequired()) {
					throw new XmlException(getNodeInfo() + " Node Attribute: "
							+ this.getName() + " is required but empty");
				}
			}
		}
	}

	private class SeedNodeParser extends Parser {
		public SeedNodeParser(boolean required) {
			super(
					required,
					false,
					NODE_PARSER_seed,
					"DefaultParser. Random number generator seed of this Element. Overrides default seeding behaviour.");
		}

		public SeedNodeParser() {
			super(
					false,
					false,
					NODE_PARSER_seed,
					"DefaultParser. Random number generator seed of this Element. Overrides default seeding behaviour.");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();

			if (text != null && !text.isEmpty()) {
				seed = StaticHelper.parseLongTextContent(getNodeInfo(), node);
				setExecuted();
			} else {
				seed = null;
				if (isRequired()) {
					throw new XmlException(getNodeInfo() + " <"
							+ this.getName() + "> Node is required but empty");
				}
			}
		}
	}

	private class RNGNodeParser extends Parser {

		public RNGNodeParser(boolean required, Element parent) {

			super(
					required,
					false,
					NODE_PARSER_rng,
					parent,
					"DefaultParser. Name of random number generator class to be used for calculations in this element.  Example: com.en.myRNG");

		}

		// public RNGNodeParser(boolean required) {
		// this(required, null);
		// }

		// public RNGNodeParser() {
		// this(false);
		//
		// }

		@Override
		protected void parse(Node node) throws XmlException {
			String nodeName = StaticHelper.getNodeNameAttr(node,
					this.getParent());
			if (isRequired()
					&& (nodeName == null || nodeName.isEmpty() || nodeName
							.equalsIgnoreCase(Constants.DEFAULT))) {
				throw new XmlException(getNodeInfo() + " <" + this.getName()
						+ "> Node is required but Name Attribute is empty");

			}

			rngName = nodeName;
			// Test RNG existence if not default should be used
			if (rngName != null && !rngName.isEmpty()
					&& !rngName.equalsIgnoreCase(Constants.DEFAULT)) {

				RandomFactory.instance().getRNGClass(node, this.getParent());
			} else {
				// if empty, or default set to Constants.DEFAULT
				rngName = getRngName();
			}

			// if this is the first element in the element tree

			if (this.getParent().getParent() == null) {
				if (rngName == Constants.DEFAULT) {
					throw new XmlException(
							getNodeInfo()
									+ " <"
									+ this.getName()
									+ "> This is the Root element. the root element requires a  &lt;rng name=\"...\">&lt;/rng> node. This RNG will be the default RNG");
				}
			}
		}
	}

	private class SizeNodeParser extends Parser {

		public SizeNodeParser(boolean required) {
			super(required, false, NODE_PARSER_size,
					"DefaultParser. Size of this element. For tables this is the number of rows.");
		}

		public SizeNodeParser() {
			super(false, false, NODE_PARSER_size,
					"DefaultParser. Size of this element. For tables this is the number of rows.");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				setSize(StaticHelper.parseLongTextContent(getNodeInfo(), node));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getNodeInfo());
					errMsg.append('<');
					errMsg.append(node.getNodeName());
					errMsg.append("> must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">10</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}
		}
	}

	private class MinNodeParser extends Parser {

		public MinNodeParser(boolean isRequired) {
			super(
					isRequired,
					false,
					NODE_PARSER_min,
					"DefaultParser. Min Value of this element. Can be used by Field description or in Plugins.");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				setMin(StaticHelper.parseLongTextContent(getNodeInfo(), node,
						0, Long.MAX_VALUE));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getNodeInfo());
					errMsg.append('<');
					errMsg.append(node.getNodeName());
					errMsg.append("> must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">10</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}
		}
	}

	private class MaxNodeParser extends Parser {

		public MaxNodeParser(boolean isRequired) {
			super(
					isRequired,
					false,
					NODE_PARSER_max,
					"DefaultParser. Max Value of this element. Can be used by Field description or in Plugins.");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				setMax(StaticHelper.parseLongTextContent(getNodeInfo(), node,
						0, Long.MAX_VALUE));
			} else {
				if (this.isRequired()) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append(getNodeInfo());
					errMsg.append('<');
					errMsg.append(node.getNodeName());
					errMsg.append("> must not be empty. Example: <");
					errMsg.append(node.getNodeName());
					errMsg.append(">10</");
					errMsg.append(node.getNodeName());
					errMsg.append('>');
					throw new XmlException(errMsg.toString());
				}
			}

		}

	}
}
