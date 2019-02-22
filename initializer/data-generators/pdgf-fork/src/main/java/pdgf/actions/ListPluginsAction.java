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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import pdgf.core.Element;
import pdgf.core.dataGenerator.scheduler.Scheduler;
import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Distribution;
import pdgf.plugin.Generator;
import pdgf.plugin.Output;
import pdgf.util.ClassLoading.ClassFinder;

public class ListPluginsAction extends Action {

	private ClassFinder classFinder = new ClassFinder();

	public ListPluginsAction() {
		super(
				"plugins",
				"<type>",
				"displays available plugins. Permitted values for <type>: { all | generator | distribution | output | scheduler | random}",
				1, 1);
	}

	@Override
	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException {
		checkParamQuantity(tokens);
		List<Class<?>> plugins = null;
		Class c;
		if (tokens[1] == null || tokens[1] == "")
			throw new InvalidArgumentException("No parameters given.");
		// this is a special case
		if (tokens[1].equalsIgnoreCase("all")) {
			c = Element.class;
			plugins = getPlugins(c);
			printPlugins(plugins, c, true, true);

			c = AbstractPDGFRandom.class;
			plugins = getPlugins(c);
			printPlugins(plugins, c, false, false);
		} else if (tokens[1].equalsIgnoreCase(Generator.class.getSimpleName())) {
			c = Generator.class;

		} else if (tokens[1].equalsIgnoreCase(Distribution.class
				.getSimpleName())) {
			c = Distribution.class;

		} else if (tokens[1].equalsIgnoreCase(Scheduler.class.getSimpleName())) {
			c = Scheduler.class;

		} else if (tokens[1].equalsIgnoreCase(AbstractPDGFRandom.class
				.getSimpleName()) || tokens[1].equalsIgnoreCase("random")) {

			c = AbstractPDGFRandom.class;

		} else if (tokens[1].equalsIgnoreCase(Output.class.getSimpleName())) {
			c = Output.class;

		} else {
			throw new InvalidArgumentException("Unknown command.");
		}
		plugins = getPlugins(c);
		printPlugins(plugins, c, true, false);
	}

	/**
	 * Get all Subclasses of a given Class. Searches the complete class path.
	 * 
	 * @param c
	 *            Superclass of subclasses to search for.
	 * @return Vector of classes
	 */
	private List<Class<?>> getPlugins(Class c) {
		return classFinder.findSubclasses(c.getName());
	}

	private void printPlugins(List<Class<?>> plugins, Class pluginSuperclass,
			boolean header, boolean withCoreClasses) {
		String pluginClassName;

		Class<?> currentPluginClass;
		if (header) {
			log.info(String.format("%n%-18s| %s\n", "Plugin type",
					"Plugin name"));
		}
		HashSet<String> dupplicateDetector = new HashSet<String>();

		// print core classes in pdgf.core.dbSchema first, if requested
		if (withCoreClasses) {
			log.info("Core:\n");

			for (Iterator<Class<?>> iterator = plugins.iterator(); iterator
					.hasNext();) {
				currentPluginClass = (Class<?>) iterator.next();
				pluginClassName = currentPluginClass.getName();

				// only classes in pdgf.core.dbSchema
				if (pluginClassName.startsWith(Project.class.getPackage()
						.getName())) {
					if (!dupplicateDetector.contains(pluginClassName)) {
						dupplicateDetector.add(pluginClassName);

						log.info(String.format("%-18s| %s", currentPluginClass
								.getSuperclass().getSimpleName(),
								pluginClassName));
					}
				}
			}

		}

		// print only classes of pdgf.core.plugin package
		dupplicateDetector = new HashSet<String>();
		for (Iterator<Class<?>> iterator = plugins.iterator(); iterator
				.hasNext();) {
			currentPluginClass = (Class<?>) iterator.next();
			pluginClassName = currentPluginClass.getName();

			// only classes in pdgf.core.dbSchema
			if (pluginClassName.startsWith(Generator.class.getPackage()
					.getName())) {
				if (!dupplicateDetector.contains(pluginClassName)) {
					dupplicateDetector.add(pluginClassName);

					log.info(String.format("%-18s| %s", currentPluginClass
							.getSuperclass().getSimpleName(), pluginClassName));
				}
			}
		}

		// finally show only plugins, not their superclass. Also do not show
		// Classes
		// in pdgf.core.dbSchema or pdgf.core.plugin
		dupplicateDetector = new HashSet<String>();
		for (Iterator<Class<?>> iterator = plugins.iterator(); iterator
				.hasNext();) {
			currentPluginClass = (Class<?>) iterator.next();
			pluginClassName = currentPluginClass.getName();

			// show only plugins, not their superclass. Also do not show Classes
			// in pdgf.core.dbSchema

			if (!pluginClassName.equals(pluginSuperclass.getName())
					&& !pluginClassName.startsWith(Project.class.getPackage()
							.getName())
					&& !pluginClassName.startsWith(Generator.class.getPackage()
							.getName())) {
				if (!dupplicateDetector.contains(pluginClassName)) {
					dupplicateDetector.add(pluginClassName);

					log.info(String.format("%-18s| %s", currentPluginClass
							.getSuperclass().getSimpleName(), pluginClassName));
				}
			}
		}
	}
}
