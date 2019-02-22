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
import pdgf.core.dataGenerator.scheduler.SchedulerFactory;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.XmlException;
import pdgf.output.OutputFactory;
import pdgf.plugin.AbstractScheduler;
import pdgf.plugin.Output;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;

import java.util.LinkedList;

/**
 * Project represents a PDGF DataGeneration Project. The project configuration
 * must be provided via an xml subtree:
 * 
 * <br/>
 * <br/>
 * &lt;project name="projectname"><br/>
 * <BLOCKQUOTE> &lt;seed> a long value &lt;/seed><br/>
 * <br/>
 * &lt;defaultRNG>{packageName.className /empty}&lt;/defaultRNG><br/>
 * <br/>
 * &lt;scaleFactor>1&lt;/scaleFactor><br/>
 * <br/>
 * 
 * &lt;output name="CSVRowOutput"> <BLOCKQUOTE> ... </BLOCKQUOTE> &lt;/output><br/>
 * <br/>
 * 
 * &lt;tables> <BLOCKQUOTE> &lt;table name="tablename1"> ... &lt;/table> <br/>
 * &lt;table name="tablename2"> ... &lt;/table> <br/>
 * ... </BLOCKQUOTE> &lt;/tables><br/>
 * </BLOCKQUOTE> &lt;/Project>
 * 
 * @author Michael Frank
 * @version 1.0 13.10.2009
 */
public class Project extends Element<Table, Element> {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Project.class);
	private static final String ATTR_PARSER_Version = "targetVersion";

	public static final String NODE_PARSER_scaleFactor = "scaleFactor";
	public static final String NODE_PARSER_output = "output";
	public static final String NODE_PARSER_tables = "tables";
	public static final String NODE_PARSER_scheduler = "scheduler";
	// Project database attributes
	private Integer scaleFactor = Constants.INT_NOT_SET;

	// Project node configuration attributes

	private int workers = Constants.INT_NOT_SET;
	// Project Generated data output
	private Output output = null;

	private String targetVersion = null; // Programmversion project config File
	// was created for.

	private AbstractScheduler scheduler = null;

	private boolean isInitialized = false;

	private boolean projectConfigLoaded = false;

	// *******************************************************************
	// constructors
	// *******************************************************************

	/**
	 * Creates a new Project with the configuration provided in rootNode. Root
	 * node represtens the config.xml�s first tag
	 * 
	 * @param rootNode
	 *            first tag in config file
	 * @throws XmlException
	 */
	public Project(Node rootNode) throws XmlException {
		this();
		parseConfig(rootNode);
	}

	/**
	 * Creates a new Project empty project
	 * @throws XmlException 
	 */
	public Project() throws XmlException {
		super("Project", "This is the Project");
	}

	// *******************************************************************
	// parse xml configuration
	// *******************************************************************
	/**
	 * Parses the given subtree of the config file. Fills the fields of this
	 * class with the data contained in the config file.
	 * 
	 * @param node
	 *            the node marking the subtree containing the configuration for
	 *            this project
	 * @throws XmlException
	 *             error while parsing the relevant part of the xml file
	 *             responsible for this project
	 */
	@Override
	public synchronized void parseConfig(Node node) throws XmlException {
		if (node.getNodeName().equalsIgnoreCase("nodeConfig")) {
			log.info("Config File is <node> configuration...");
			parseNodeConfig(node);
		} else if (node.getNodeName().equalsIgnoreCase("project")) {
			projectConfigLoaded = false;
			log.info("Config File is <project> configuration...");
			super.parseConfig(node);

			initializeReferences();
			projectConfigLoaded = true;
		} else {
			throw new XmlException(
					"XML file was neither a <project> nor a <nodeConfig> configuration file. The File must start with one of the two possible nodes. First node in this file was: <"
							+ node.getNodeName() + ">");
		}
	}

	public boolean projectConfigLoaded() {
		return projectConfigLoaded;
	}

	/**
	 * Called automatically after parsing project config File
	 * 
	 * @throws XmlException
	 */
	private void initializeReferences() throws XmlException {
		log.debug("\n initialize references....");
		Table[] tables = this.getChilds();
		Table t;
		Field[] fields;

		for (int tableNo = 0; tableNo < tables.length; tableNo++) {
			t = tables[tableNo];
			fields = t.getChilds();
			for (int fieldNo = 0; fieldNo < fields.length; fieldNo++) {
				fields[fieldNo].initializeReferences();
			}
		}
		log.debug("Initializing references....successfully done");

	}

	public synchronized boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * initializes the Project. For concurency reasons every worker has his own
	 * random number generator. Therfore this method is called, each time you
	 * change the number of participating workers. Initializes all Childs and
	 * method.<br/>
	 * Requirements:
	 * <ul>
	 * <li>project config is loaded (xml description of db schema)</li>
	 * <li>node counfiguration is loaded or set</li>
	 * <li>number of workers is set by console or automatically by datagenerator
	 * </li>
	 * </ul>
	 * <br/>
	 * <br/>
	 * This means this method can only be called short before start of data
	 * generation.<br/>
	 * Project.initialize() is called by Generator.initialize();
	 * 
	 * @param workersOverride
	 *            if workersOverride >=1 set this.workerCount =workersOverride;
	 * @throws ConfigurationException
	 * @throws XmlException
	 */
	@Override
	public synchronized void initialize(int workersOverride)
			throws ConfigurationException, XmlException {
		isInitialized = false;

		if (!projectConfigLoaded) {
			throw new ConfigurationException(
					"Cannot initialize Project before a valid Project configuration is provided via XML file");
		}

		if (workersOverride >= 1) {
			workers = workersOverride;
		}

		if (workers == Constants.INT_NOT_SET || workers <= 0) {
			throw new ConfigurationException(
					"Number of workers not set or < 0 count: " + workers);
		}
		// all ok call super method, init per thread RNGs and call init on
		// childs
		super.initialize(workers);
		isInitialized = true;

		// initReferences(); Called automatically after parsing project config
		// File
	}

	/**
	 * Parses the given subtree of the config file. Fills the fields of this
	 * class with the data contained in the config file.
	 * 
	 * @param node
	 *            the node marking the subtree containing the configuration for
	 *            this project
	 * @throws XmlException
	 *             error while parsing the relevant part of the xml file
	 *             responsible for this project
	 */
	private synchronized void parseNodeConfig(Node node) throws XmlException {

		if (!node.getNodeName().equalsIgnoreCase("nodeConfig")) {
			throw new XmlException(
					"nodeConfig File must start with a <nodeConfig> node");
		}
		NodeList childs = node.getChildNodes();

		// iterate over <nodeConfig> child nodes
		Node child = null;
		String nodeName;
		for (int i = 0; i < childs.getLength(); i++) {
			child = childs.item(i);
			nodeName = child.getNodeName();
			if (nodeName.equalsIgnoreCase("workers")) {
				workers = StaticHelper.parseIntTextContent(getNodeInfo(),
						child, Integer.MAX_VALUE);
			}
		}

	}

	/**
	 * Sets workercount
	 * 
	 * @param workers
	 * @throws ConfigurationException
	 * @throws XmlException
	 */
	public synchronized void setWorkers(int workers)
			throws ConfigurationException, XmlException {
		this.workers = workers;
		// this.initialize(-1); //workers already set!
	}

	public int getWorkers() {
		return workers;
	}

	public Output getOutput() {
		return output;
	}

	// *******************************************************************
	// Getter and Setters
	// *******************************************************************

	/**
	 * sets the scalefactor. Used to determine the final desired row count for
	 * each table. final row count = scalefactor* Table.size
	 * 
	 * @param scaleFactor
	 */
	public void setScaleFactor(int scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 * gets the scale factor of this project. Used to determine the final
	 * desired row count for each table. final row count = scalefactor*
	 * Table.size
	 * 
	 * @return
	 */
	public int getScaleFactor() {
		return scaleFactor;
	}

	public long getTableID(String getChildID) {
		return getChildID(getChildID);
	}

	public long getTableID(Table t) {
		return getChildID(t);
	}

	/**
	 * @param tableID
	 *            seed for which table?
	 * @param workerID
	 *            identifies calling thread. starts at 1
	 */
	public long getTableSeed(int tableID, int workerID) {
		// Project rngs generate Table seed
		elementRngs[workerID - 1].setSeed(seed);
		return elementRngs[workerID - 1].nextLong(tableID);
	}

	public void setScheduler(AbstractScheduler scheduler) {
		this.scheduler = scheduler;
		scheduler.setParent(this);

	}

	public AbstractScheduler getScheduler() {
		return scheduler;
	}

	private void checkVersionCompatibillity() {
		// targetVersion was specified
		if (targetVersion != null && !targetVersion.isEmpty()) {
			// nothing yet, currently all configs are compatible
		} else {
			Parser attrParser = getAttrParser(ATTR_PARSER_Version);
			if (attrParser != null) {
				log.warn("Could not determine if Project config file is compatible with this program version as "
						+ attrParser.getParserNodeInfo() + " does not exist.");
			}
		}

	}

	@Override
	protected void configParsers() {
		getNodeParser(NODE_PARSER_rng).setRequired(true).setUsed(true);
		getNodeParser(NODE_PARSER_seed).setRequired(true).setUsed(true);
		addNodeParser(new TablesNodeParser(true, true, this));
		addNodeParser(new ScaleNodeParser(true, true, this));
		addNodeParser(new OutputNodeParser(true, true, this));
		addNodeParser(new SchedulerNodeParser(true, true, this));
		addAttrParser(new VersionAttrParser(false, true, this));

	}

	/**
	 * Program Version config file was created for.
	 * 
	 * @return null if unknown
	 */
	public String getConfigFileTargetVersion() {
		return targetVersion;
	}

	private class VersionAttrParser extends Parser<Project> {

		public VersionAttrParser(boolean required, boolean used, Project parent) {
			super(required, used, ATTR_PARSER_Version, parent,
					"Specifies program version a config file was created for.(optional)");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			targetVersion = node.getTextContent();
			checkVersionCompatibillity();
		}

	}

	private class ScaleNodeParser extends Parser<Project> {

		public ScaleNodeParser(boolean required, boolean used, Project parent) {
			super(required, used, NODE_PARSER_scaleFactor, parent,
					"Scale Factor for Table size. Total rows = scaleFactor * size");
		}

		@Override
		protected void parse(Node node) throws XmlException {
			scaleFactor = StaticHelper.parseIntTextContent(getNodeInfo(), node,
					Integer.MAX_VALUE);
		}
	}

	private class OutputNodeParser extends Parser<Project> {

		public OutputNodeParser(boolean required, boolean used, Project parent) {
			super(
					required,
					used,
					NODE_PARSER_output,
					parent,
					"Specifies the output plugin and its configuration to be used for output of generated value. Required child nodes are specified by the plugin.");
		}

		@Override
		protected void parse(Node node) throws XmlException {

			output = OutputFactory.instance().getRowOutput(node,
					this.getParent());
		}
	}

	private class SchedulerNodeParser extends Parser<Project> {

		public SchedulerNodeParser(boolean required, boolean used,
				Project parent) {
			super(
					required,
					used,
					NODE_PARSER_scheduler,
					parent,
					"Specifies the scheduler plugin and its configuration to be used for scheduling the work between workers. Required child nodes are specified by the plugin.");
		}

		@Override
		protected void parse(Node node) throws XmlException {

			setScheduler(SchedulerFactory.instance().getScheduler(node,
					this.getParent()));
		}
	}

	private class TablesNodeParser extends Parser<Project> {

		public TablesNodeParser(boolean required, boolean used, Project parent) {
			super(required, used, NODE_PARSER_tables, parent,
					"Node containing all Tables of this Project");

		}

		@Override
		protected void parse(Node node) throws XmlException {

			if (!node.hasChildNodes()) {
				throw new XmlException(
						getNodeInfo()
								+ " <tables> does not have any child nodes! Please add at minimum one  <table>..</table> node");
			} else {
				NodeList tableNodes = node.getChildNodes();
				int tableCount = tableNodes.getLength();
				/*
				 * // tableCount/2 because dom tree contains a #text node before
				 * // and // after each Element_Node. setChilds(new
				 * Table[tableCount / 2]);
				 */

				Node currentTableNode = null;

				LinkedList<Table> tables = new LinkedList<Table>();
				Table t = null;
				for (int j = 0; j < tableCount; j++) {
					currentTableNode = tableNodes.item(j);
					if (currentTableNode.getNodeType() == Node.ELEMENT_NODE) {
						log.debug("current table: "
								+ StaticHelper.getNodeNameAttr(
										currentTableNode, this.getParent()));
						t = new Table(this.getParent());
						t.parseConfig(currentTableNode);
						tables.add(t);
						// try {
						// addChild(t, pos);
						// } catch (InvalidElementExeption e) {
						// throw new XmlExecption(getNodeInfo() + "<"
						// + currentTable.getNodeName() + "> "
						// + e.getMessage());
						// }
						// pos++;
					}
				}

				// now final size of childs (tables) is known. Initialzie it.
				setChilds(tables.size(), t.getClass());
				log.info("Found " + tables.size() + " tables");
				int pos = 0;// position counter in tables array
				// store in array (array is used over linked list for
				// performance reasons)
				for (Table ta : tables) {
					try {
						addChild(ta, pos++);
					} catch (InvalidElementException e) {
						error("<" + currentTableNode.getNodeName() + "> "
								+ e.getMessage());

					}
				}
			}
		}
	}
}
