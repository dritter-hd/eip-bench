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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import pdgf.Controller;
import pdgf.core.dataGenerator.DataGenerator;
import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.InvalidStateException;
import pdgf.core.exceptions.XmlException;

public abstract class Action {
	protected final org.slf4j.Logger log = LoggerFactory.getLogger(Action.class);

	// description of command and parameters
	protected final String description;

	protected final String parameters;
	// required parameter count of a command
	protected final int parametercount;
	protected final int parametercountMax;
	// name of command
	protected final String command;

	// pointers for the current datagenerator and project
	protected DataGenerator dataGen = Controller.getInstance()
			.getDataGenerator();
	protected Project project = Controller.getInstance().getProject();

	/**
	 * Constructor used by subclasses to populate variables
	 * 
	 * @param command
	 *            the command for executing this action
	 * @param parameters
	 *            the possible parameters of the command
	 * @param description
	 *            the description of the command
	 * @param parametercount
	 *            the minimal number of parameters
	 * @param parametercountMax
	 *            the maximal number of parameters
	 * @param className
	 *            the Class object of the instance for logging
	 */
	public Action(String command, String parameters, String description,
			int parametercount, int parametercountMax) {
		this.command = command;
		this.description = description;
		this.parametercount = parametercount;
		this.parameters = parameters;
		this.parametercountMax = parametercountMax;
	}

	/**
	 * Method to run command of this class
	 * 
	 * @param tokens
	 *            the command and its parameters
	 * @return true if successful, else false
	 * @throws XmlException
	 * @throws ConfigurationException
	 * @throws InvalidArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ClassNotFoundException
	 * @throws InvalidElementException
	 * @throws InvalidStateException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public abstract void execute(String[] tokens) throws XmlException,
			ConfigurationException, InvalidArgumentException,
			FileNotFoundException, ParserConfigurationException, SAXException,
			IOException, ClassNotFoundException, InvalidElementException,
			InvalidStateException, InstantiationException,
			IllegalAccessException;

	/**
	 * Full Usage Description of this command commandName +
	 * parametersDescription + command description
	 */
	@Override
	public String toString() {
		return this.command + this.parameters + this.description;
	}

	/**
	 * Description text for command
	 * 
	 * @return
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * number needed parameters for this command
	 * 
	 * @return
	 */
	public int getParamCountMin() {
		return this.parametercount;
	}

	/**
	 * max number of allowed parameters for this command
	 * 
	 * @return
	 */
	public int getParamCountMax() {
		return this.parametercountMax;
	}

	/**
	 * command's name
	 * 
	 * @return
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Parameters description text
	 * 
	 * @return
	 */
	public String getParametersDescription() {
		return parameters;
	}

	/**
	 * Initializes DataGenerator class with fully confiugred Project class
	 * containing all required information (after parsing of all config files is
	 * done) This must not be called if you plan to change some values like
	 * workercount or things like that! Only if you finished configuring the
	 * project you may call this method. This method is called automatically
	 * before execution of the "start" command.
	 * 
	 * 
	 * @throws ConfigurationExecption
	 * @throws XmlExecption
	 */
	protected void initialize() throws ConfigurationException, XmlException {
		dataGen.initialize(project);
	}

	protected void checkStartPreconditions() throws InvalidStateException {
		if (project == null) {
			throw new InvalidStateException(
					"ERROR! Command: \""
							+ new StartAction().getCommand()
							+ "\" cannot be executed because: ProjectConfig Xml file and NodeConfig Xml file were not loaded");
		} else {

			// project conf can not be loaded, because project requires a name!
			if (project.getName() == null) {
				throw new InvalidStateException(
						"ERROR! Command: \""
								+ new StartAction().getCommand()
								+ "\" cannot be executed because: ProjectConfig Xml file was not loaded! Please execute:\n"
								+ new LoadConfigAction().getCommand());
			}

		}
	}

	/**
	 * Checks if param quantity provided by user matches the quantity required
	 * by the command.
	 * 
	 * @param command
	 *            command to check for
	 * @param count
	 *            user provided param count
	 * @return True if correct
	 * @throws InvalidArgumentException
	 */
	protected void checkParamQuantity(String[] tokens)
			throws InvalidArgumentException {

		if (tokens != null) {
			int count = tokens.length - 1;
			if (count > 0 && parametercountMax == 0) {
				throw new InvalidArgumentException(
						"No parameters allowed for this command");
			} else if (count < parametercount) {
				throw new InvalidArgumentException(
						"Parameters are missing. This command requires at least "
								+ parametercount + " parameters");
			} else if (count > parametercountMax) {
				throw new InvalidArgumentException(
						"To much parameters for this command. This command only supports "
								+ parametercountMax + " parameters");
			}
		}
	}
}
