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

import java.io.IOException;
import java.util.HashMap;

import pdgf.Controller;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.InvalidStateException;
import pdgf.core.exceptions.XmlException;

public class ShowHelpAction extends Action {

	public ShowHelpAction() {
		super(
				"help",
				"",
				"displays all available commands with a short description and needed parameters",
				0, 1);
	}

	/**
	 * Starts the Data Generation process
	 * 
	 * @throws XmlExecption
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ConfigurationExecption
	 */
	@Override
	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException, ConfigurationException, IOException,
			InvalidStateException, InstantiationException,
			IllegalAccessException {
		checkParamQuantity(tokens);
		if (tokens == null)
			throw new InvalidArgumentException("No command given");

		HashMap<String, Class<Action>> commands = Controller.getInstance()
				.getCommandMap();
		// print only help for one command
		if (tokens.length > 1) {
			if (tokens[1].equalsIgnoreCase("exit")) {
				log.info("quits the shell, stops all workers and exits programm\n");
			} else if (tokens[1].equalsIgnoreCase("noShell")) {
				log.info("non interactive mode: disables the shell. Also activates automatic exit: closeWhenDone true\n");
			} else {
				if (!commands.containsKey(tokens[1]))
					throw new InvalidArgumentException(
							"The command "
									+ tokens[1]
									+ " is not known. Enter help to see a list of commands");
				log.info(((Action) commands.get(tokens[1]).newInstance())
						.getDescription());
			}
		} else {
			String PROGRAM_NAME = Controller.getInstance().getProgramName();
			char CMD_PREFIX = Controller.getInstance().getCmdPrefix();

			StringBuilder sb = new StringBuilder();
			sb.append(PROGRAM_NAME.toLowerCase() + ".jar \n");
			sb.append(CMD_PREFIX);
			sb.append("exit: quits " + PROGRAM_NAME
					+ " shell, stops all workers and exits programm\n");
			sb.append(CMD_PREFIX);
			sb.append("noShell: non interactive mode: disables "
					+ PROGRAM_NAME
					+ " shell. Also activates automatic exit: closeWhenDone true\n");

			for (Class<?> actionClass : commands.values()) {
				Action tempClass = (Action) actionClass.newInstance();
				sb.append(CMD_PREFIX);
				sb.append(tempClass.getCommand() + ": "
						+ tempClass.getDescription() + "\n");
			}

			sb.append("\n\nNote: some commands depend on other commands. For example: you can not start datageneration if the config files are not loaded. The command line parameters are processed sequentially. So the ");
			log.info(sb.toString());
		}
	}
}
