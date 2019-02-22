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

import java.io.Serializable;

import org.w3c.dom.Node;

import pdgf.core.exceptions.XmlException;

/**
 * A parser is responsible for Pasring a specific node of an XML DOM Tree
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 * @param <Parent>
 *            Parent element of this parser
 */
public abstract class Parser<Parent extends Element> implements Serializable {
	private String nodeName;
	private Parent parent = null;
	private boolean wasExecuted;
	private boolean isRequired;
	private boolean isUsed;
	private String description = "";
	private boolean isAttrParser = false;

	public Parser(boolean isRequired, boolean isUsed, String nodeName,
			String description) {
		this(isRequired, isUsed, nodeName, null, description);
	}

	public Parser(boolean isRequired, boolean used, String nodeName,
			Parent parent, String description) {
		super();
		this.isUsed = used;
		this.parent = parent;
		this.nodeName = nodeName;
		this.isRequired = isRequired;
		this.description = description;
	}

	// public Parser(boolean required, String nodeParserRng, Element parent2) {
	// // TODO Auto-generated constructor stub
	// }

	abstract protected void parse(Node node) throws XmlException;

	public void parseNode(Node node) throws XmlException {

		parse(node);
		this.wasExecuted = true;
	}

	public String getName() {

		return this.nodeName;
	}

	/**
	 * This parser was executed (he parsed successfully a node)
	 * 
	 * @return the wasExecuted
	 */
	public boolean isExecuted() {
		return this.wasExecuted;
	}

	/**
	 * Sets executed to true
	 */
	public void setExecuted() {
		this.wasExecuted = true;
	}

	/**
	 * Parser is required. Config file is not valid until a node for this parser
	 * exists with a correct value
	 * 
	 * @return the isRequired
	 */
	public boolean isRequired() {
		return this.isRequired;
	}

	/**
	 * Parser is required. If isRequired == true, setUsed is also set to true,
	 * Config file is not valid until a node for this parser exists with a
	 * correct value
	 * 
	 * @param isRequired
	 *            the isRequired to set
	 */
	public Parser<Parent> setRequired(boolean isRequired) {
		this.isRequired = isRequired;
		if (isRequired) {
			this.isUsed = true;
		}
		return this;
	}

	public Parent getParent() {
		return this.parent;
	}

	public Parser<Parent> setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Set if this parser is used (supported) by his Parent
	 * 
	 * @param isUsed
	 * @return
	 */
	public Parser<Parent> setUsed(boolean isUsed) {
		this.isUsed = isUsed;
		return this;
	}

	public String getParserNodeInfo() {
		if (parent == null) {
			return (getName() + " ");
		} else if (isAttrParser) {
			return (parent.getNodeInfo() + " Attribute: " + getName() + " ");
		} else {
			return (parent.getNodeInfo() + "<" + getName() + "> ");
		}
	}

	public void setIsAttrParser(boolean isAttrParser) {
		this.isAttrParser = isAttrParser;
	}

	public boolean isAttrParser() {

		return isAttrParser;
	}

	/**
	 * Throws an XmlExeption with specified message and as prefix the place of
	 * occurrence (the full path to the Node this parser is responsible for)
	 * 
	 * @param msg
	 * @throws XmlException
	 */
	public void error(String msg) throws XmlException {

		throw new XmlException(getParserNodeInfo() + msg);

	}

	public boolean isUsed() {
		return isUsed;
	}

}
