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
package pdgf.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pdgf.Controller;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.NotSupportedException;
import pdgf.core.exceptions.XmlException;
import pdgf.util.Constants;

public class LoadConfigAction extends Action {

	public LoadConfigAction() {
		super(
				"load",
				"<Filename>",
				"read and Parse a XML configuration file (node or project config).\nNOTE: Replaces previously imported configuration without asking.",
				1, 1);
	}

	/**
	 * Parse the configuration xml file
	 * 
	 * @param tokens
	 *            Parameter array containing the file name for the xml config
	 *            file at tokens[1], and the commandname at tokens[0]
	 * @throws XmlException
	 *             error while parsing the configuration file
	 */

	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException, ParserConfigurationException,
			SAXException, IOException {
		checkParamQuantity(tokens);
		if (!dataGen.isStarted()) {
			if (tokens == null || tokens.length != 2)
				throw new InvalidArgumentException(
						"Number of arguments is wrong");

			File xmlConfigFile = new File(tokens[1]);

			// if conf file not found, search in default config dir
			if (!checkFile(xmlConfigFile)) {
				log.info("Config file not found. Searching for file in standard config file dir: "
						+ Constants.DEFAULT_CONFIG_FILE_DIR);
				xmlConfigFile = new File(Constants.DEFAULT_CONFIG_FILE_DIR
						+ File.separatorChar + tokens[1]);
			}

			if (checkFile(xmlConfigFile)) {
				parseXmlFile(xmlConfigFile);
			} else {
				throw new FileNotFoundException("Config file " + tokens[1]
						+ " could not be found in "
						+ Constants.DEFAULT_CONFIG_FILE_DIR);
			}
		} else {
			throw new NotSupportedException(
					"Cannot load config file while generator is running");
		}
	}

	/**
	 * Parse Project and Node xml files. This method can handle both types.
	 * Project xml file must start with a &ltProject> tag. Node xml file must
	 * start with a &ltnodeConfig> tag.
	 * 
	 * @param xmlFile
	 *            the xml file to parse
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private void parseXmlFile(File xmlFile) throws XmlException,
			ParserConfigurationException, SAXException, IOException {

		if (!dataGen.isStarted()) {
			log.info("Parsing configuration file: " + xmlFile.getAbsolutePath());

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			log.info("parsing config file data...");

			// parse input file to get a Document object
			Document doc = db.parse(xmlFile);
			doc.setStrictErrorChecking(true);

			Controller.getInstance().getProject()
					.parseConfig(doc.getFirstChild());
			log.info("Configuration File \"" + xmlFile.getName()
					+ "\" imported without errors");
			log.info("Configuration File imported without errors");
		} else {
			throw new NotSupportedException(
					"Cannot load config file while generator is running");
		}
	}

	/**
	 * Check file existence and if file can be read
	 * 
	 * @param f
	 *            the file to check
	 * @return true if all requirements are met
	 */
	private boolean checkFile(File f) {
		return (f != null && f.exists() && f.isFile() && f.canRead());
	}
}
