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
package pdgf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import pdgf.actions.Action;
import pdgf.actions.LoadPluginsAction;
import pdgf.actions.StopAction;
import pdgf.core.dataGenerator.DataGenerator;
import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.InvalidStateException;
import pdgf.core.exceptions.XmlException;
import pdgf.util.StaticHelper;
import pdgf.util.ClassLoading.ClassFinder;
import pdgf.util.ClassLoading.PluginLoader;

/**
 * Front controller of the PDGF framework. If you plan to use PDGF without the
 * shell (eg as plugin or with a gui) execute your commands with
 * {@linkplain Controller#executeCommand(Command, String...)}
 * 
 * <br/>
 * Outputs of this controller can be controlled with
 * {@linkplain Controller#setControllerMessagesOutputStream(PrintStream)}
 * Outputs of other classes can be configured with log4j.properties. You may
 * consider writing your own custom log4j appender to collect messages of
 * Generator classes (Datagenerator, scheduler, worker, generator plugins) <br/>
 * If used from shell, config files can be loaded as commandline parameters.
 * After starting a own Shell is loaded. This controller can also be used
 * (wrapped) by a gui.
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class Controller extends Observable implements Observer {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);
	private static final String Log4JCFG_File = "Log4j.properties";

	private static Controller me;

	private Project project;
	private DataGenerator dataGen;
	private boolean shellRunning = false;

	private ArrayList<String> pluginDirs = null;
	private HashMap<String, Class<Action>> commands = null;

	private final char CMD_PREFIX = '-';
	private final String PROGRAM_NAME = "PDGF";
	private final String PROMPT = PROGRAM_NAME + ":> ";

	private final String HELLO_MSG = "################################################################################\n"
			+ "\t\t\t "
			+ PROGRAM_NAME
			+ " v"
			+ StaticHelper.VERSION
			+ "\n"
			+ "\tParallel Data Generation Framework for Database Benchmarks\n\t\t\tAuthor: Michael Frank\n"
			+ "for usage help start with java -jar "
			+ PROGRAM_NAME.toLowerCase()
			+ ".jar -help or type \"help\" in: "
			+ PROMPT
			+ "\n###############################################################################";

	private final String ERROR = "ERROR! ";

	private ClassFinder classFinder;

	/**
	 * invoked by commandline
	 * 
	 * @param args
	 * @throws XmlException
	 * @throws XmlExecption
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static void main(String[] args) throws XmlException {
		Controller controller = new Controller();
		controller.startShell(args);
	}

	// *******************************************************************
	// constructors
	// *******************************************************************

	/**
	 * Front controller of the PDGF framework. If you plan to use PDGF without
	 * the shell (eg as plugin or with a gui) execute your commands with
	 * {@linkplain Controller#executeCommand(Command, String...)}
	 * 
	 * @throws XmlException
	 * 
	 */
	public Controller() throws XmlException {
		me = this;

		dataGen = new DataGenerator(this);
		dataGen.addObserver(this);
		project = new Project();

		classFinder = new ClassFinder(PluginLoader.getClassLoader());
		commands = buildActionClassMap();

	}

	public static Controller getInstance() {
		return me;
	}

	/**
	 * Scans for all available action classes and builds a map (command ->
	 * ActionClassName)
	 */
	private HashMap<String, Class<Action>> buildActionClassMap() {

		HashMap<String, Class<Action>> temp = new HashMap<String, Class<Action>>();
		ClassFinder finder = classFinder;

		List<Class<?>> actionClasses = finder.findSubclasses(Action.class
				.getName());

		for (Class currentClass : actionClasses) {
			try {
				// System.out.println("Action(s): " + currentClass.getName());
				Action actionClass = (Action) currentClass.newInstance();
				// Action actionClass = (Action) currentClass.newInstance();

				// put Action class in map with command name as key
				Class<Action> overwrite = temp.put(actionClass.getCommand()
						.toLowerCase(), (Class<Action>) currentClass);
				if (overwrite != null) {
					throw new InstantiationError(ERROR
							+ "Duplicate command name '"
							+ actionClass.getCommand().toLowerCase()
							+ "' . Command: " + overwrite + " and "
							+ currentClass + " use the same command name!");
				}

				// put Action class in map with mnemonic name as key
				if (actionClass.getCommand() != null
						&& !actionClass.getCommand().isEmpty()) {

					overwrite = temp.put(
							actionClass.getCommand().toLowerCase(),
							(Class<Action>) currentClass);
					if (overwrite != null) {
						throw new InstantiationException(ERROR
								+ "Duplicate command  key '"
								+ actionClass.getCommand().toLowerCase()
								+ "' . Command: " + overwrite + " and "
								+ currentClass + " use the same mnemonic!");

					}
				}

			} catch (InstantiationException e) {
				log.debug(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return temp;
	}

	private HashMap<String, Class<?>> buildActionClassMap_() {
		HashMap<String, Class<?>> temp = new HashMap<String, Class<?>>();
		List<Class<?>> actionClasses = new ClassFinder()
				.findSubclasses(Action.class.getName());
		for (Class<?> currentClass : actionClasses) {
			try {
				Action actionClass = (Action) currentClass.newInstance();
				temp.put(actionClass.getCommand(), currentClass);
			} catch (InstantiationException e) {
				log.error(ERROR + e.getMessage());
			} catch (IllegalAccessException e) {
				log.error(ERROR + e.getMessage());
			}
		}
		return temp;
	}

	// *******************************************************************
	// shell part
	// *******************************************************************

	/**
	 * Spawns a command line shell for low level user interaction.
	 * 
	 * @param args
	 */
	public void startShell(String[] args) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		shellRunning = true; // shell is accepting commands
		String input; // stores user input

		try {
			parseCmdLineArgs(args);
		} catch (XmlException e) {
			log.error(ERROR + e.getMessage());
		} catch (ConfigurationException e) {
			log.error(ERROR + e.getMessage());
		} catch (IOException e) {
			log.error(ERROR + e.getMessage());
		} catch (InvalidArgumentException e) {
			log.error(ERROR + e.getMessage());
		} catch (InstantiationException e) {
			log.error(ERROR + e.getMessage());
		} catch (IllegalAccessException e) {
			log.error(ERROR + e.getMessage());
		} catch (ParserConfigurationException e) {
			log.error(ERROR + e.getMessage());
		} catch (SAXException e) {
			log.error(ERROR + e.getMessage());
		} catch (ClassNotFoundException e) {
			log.error(ERROR + e.getMessage());
		} catch (InvalidElementException e) {
			log.error(ERROR + e.getMessage());
		} catch (InvalidStateException e) {
			log.error(ERROR + e.getMessage());
		}

		if (!shellRunning) {
			log.info("Shell is disabled");
		} else {
			log.info(HELLO_MSG);
		}

		int errorCount = 0;
		// accept commands till a command returns false.
		while (shellRunning) {
			try {
				System.out.print("\n" + PROMPT);
				input = in.readLine();

				if (errorCount > 10) {
					log.error(ERROR
							+ " Flooding prevention! to much failed commands in a row...exiting");
					doQuitCMD();
				}
				if (input == null || input.isEmpty()) {
					errorCount++;
					log.error("Please enter a command. Enter \"help\" for a list of available commands");
				} else {
					String[] parameter = input.trim().split("\\s+");
					executeCommand(parameter);
					errorCount = 0;
				}
			} catch (IOException e) {
				log.error(ERROR + e.getMessage());
			} catch (InvalidArgumentException e) {
				errorCount++;
				log.error(ERROR + e.getMessage());
			} catch (InstantiationException e) {
				log.error(ERROR + e.getMessage());
			} catch (IllegalAccessException e) {
				log.error(ERROR + e.getMessage());
			} catch (XmlException e) {
				log.error(ERROR + e.getMessage());
			} catch (ConfigurationException e) {
				log.error(ERROR + e.getMessage());
			} catch (ParserConfigurationException e) {
				log.error(ERROR + e.getMessage());
			} catch (SAXException e) {
				log.error(ERROR + e.getMessage());
			} catch (ClassNotFoundException e) {
				log.error(ERROR + e.getMessage());
			} catch (InvalidElementException e) {
				log.error(ERROR + e.getMessage());
			} catch (InvalidStateException e) {
				log.error(ERROR + e.getMessage());
			}
		}
	}

	/**
	 * Parse command line arguments if PDGFController is operated in shell mode
	 * 
	 * @param args
	 *            commandline arguments
	 * @return true if arguments are valid
	 * @throws ConfigurationException
	 * @throws XmlException
	 * @throws XmlExecption
	 * @throws IOException
	 * @throws InvalidArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvalidStateException
	 * @throws InvalidElementException
	 * @throws ClassNotFoundException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ConfigurationExecption
	 */
	private void parseCmdLineArgs(String[] args) throws IOException,
			XmlException, ConfigurationException, InvalidArgumentException,
			InstantiationException, IllegalAccessException,
			ParserConfigurationException, SAXException, ClassNotFoundException,
			InvalidElementException, InvalidStateException {

		if (args.length == 0) {
			log.error("Configuration files were not provided as command line argument.\n Please load config manually via "
					+ PROMPT + " shell");
		} else {
			int arraypos = 0;

			while (arraypos < args.length) {
				ArrayList<String> command = new ArrayList<String>();
				if (!args[arraypos].startsWith(String.valueOf(CMD_PREFIX)))
					throw new InvalidArgumentException(
							"Arguments have to start with " + CMD_PREFIX);
				command.add(args[0].substring(1));
				arraypos++;

				// get commands parameters
				while (arraypos < args.length
						&& !args[arraypos].startsWith(String
								.valueOf(CMD_PREFIX))) {
					command.add(args[arraypos]);
					arraypos++;
				}
				executeCommand(command.toArray(new String[0]));
			}
		}
	}

	/**
	 * executes the given command. Checks execution preconditions if required
	 * and provides the command with the required parameter tokens and executes
	 * it. <br/>
	 * Available commands are in {@linkplain Controller.Command} <br/>
	 * Command parmeters must be provided as they would be vial commandline!
	 * tokens[0] must be the command name (not required) and in the follwing
	 * tokens the arguments for this command. like: tokens[0] = "load" ,
	 * tokens[1] =
	 * 
	 * 
	 * @param command
	 *            cmd to execute
	 * @param tokens
	 *            cmd parameters as array. tokens[0] contains the cmd name <br />
	 *            tokens[1] to tokens[n] : the parameters needed by this
	 *            command. If you like to load a config File tokens[1] must
	 *            contain the path and Filename to this file:
	 *            tokens[1]="configs/example.xml";
	 * 
	 * @return True, wait for next user command. False, terminate programm
	 * @throws InvalidArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvalidStateException
	 * @throws InvalidElementException
	 * @throws ClassNotFoundException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ConfigurationException
	 * @throws XmlException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlExecption
	 * @throws ConfigurationExecption
	 */
	public void executeCommand(String[] command)
			throws InvalidArgumentException, InstantiationException,
			IllegalAccessException, FileNotFoundException, XmlException,
			ConfigurationException, ParserConfigurationException, SAXException,
			IOException, ClassNotFoundException, InvalidElementException,
			InvalidStateException {

		if (command[0].equalsIgnoreCase("exit")) {
			doQuitCMD();
		} else if (command[0].equalsIgnoreCase("noShell")) {
			shellRunning = false;
			log.info("stopping shell...");
		} else {
			if (!commands.containsKey(command[0]))
				throw new InvalidArgumentException("The command " + command[0]
						+ " is not known. Enter help to see a list of commands");
			Action actionClass = (Action) commands.get(command[0])
					.newInstance();
			actionClass.execute(command);
		}
	}

	public void doQuitCMD() {
		shellRunning = false;
		try {
			new StopAction().execute(null);
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * convenience method.
	 * 
	 * @return
	 */
	public DataGenerator getDataGenerator() {
		return dataGen;
	}

	public Project getProject() {
		return project;
	}

	public ArrayList<String> getPluginDirs() {
		return pluginDirs;
	}

	public HashMap<String, Class<Action>> getCommandMap() {
		return commands;
	}

	public String getProgramName() {
		return PROGRAM_NAME;
	}

	public char getCmdPrefix() {
		return CMD_PREFIX;
	}

	public void update(Observable o, Object arg) {
		// forward messages from DataGenerator
		if (arg instanceof String) {
			log.info((String) arg);

		}
	}
}
