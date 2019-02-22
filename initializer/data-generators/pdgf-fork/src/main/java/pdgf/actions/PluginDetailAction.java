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

import java.lang.reflect.Modifier;
import java.util.ListIterator;

import pdgf.core.Element;
import pdgf.core.Parser;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.util.StaticHelper;

public class PluginDetailAction extends Action {

	public PluginDetailAction() {
		super(
				"plugin",
				"<name>",
				"displays information on a plugin. Parameter <name>: the plugin name (like myProject.generator.MyCustomGenerator)",
				1, 1);
	}

	@Override
	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException, ClassNotFoundException {
		checkParamQuantity(tokens);
		printPluginDetails(pdgf.util.ElementFactory.instance()
				.getElement(tokens[1]).getClass());
	}

	private void printPluginDetails(Class pluginClass) {
		String pluginDescription = "Error getting description";
		Element pluginInstance = null;
		String pluginClassName = pluginClass.getName();
		String tagName = "";
		log.info("Details for: " + pluginClassName);

		// do not show info on abstract plugins or interfaces
		if (pluginClass.isInterface()
				|| Modifier.isAbstract(pluginClass.getModifiers())) {
			pluginDescription = ">>is abstract or Interface<<";

			// check if pluginClass is subclass of Element
		} else if (StaticHelper.isSubClassOf(pluginClass, Element.class)) {
			try {
				pluginInstance = (Element) pluginClass.newInstance();
				pluginDescription = pluginInstance.getDescription();
				tagName = "<" + pluginInstance.getNodeTagName() + " name=\""
						+ pluginClass.getName() + "\">";

				log.info("Tag usage: " + tagName);
				log.info("\nDescription:\n------------");
				log.info(pluginDescription);

				log.info("\nSupported tag attributes:\n-------------------------");
				log.info(String.format("%-4s|%-4s|%-18s |%s%n", "Used", "Req.",
						"Attribute name", "Description"));
				for (ListIterator<Parser> iterator = pluginInstance
						.getAttrParserList().listIterator(); iterator.hasNext();) {
					Parser p = (Parser) iterator.next();

					log.info(String.format(" %-4s%-5s%-19s%s", p.isUsed() ? "t"
							: "f", p.isRequired() ? "t" : "f", String.format(
							"%s=\"..\"", p.getName()), p.getDescription()));

				}

				log.info("\nRegistred Child Node Parsers:\n-----------------------------");
				log.info(String.format("%-4s|%-4s|%-18s |%s%n", "Used", "Req.",
						"Tag name", "Description"));
				for (ListIterator<Parser> iterator = pluginInstance
						.getNodeParserList().listIterator(); iterator.hasNext();) {
					Parser p = (Parser) iterator.next();
					log.info(String.format(" %-4s%-5s%-19s%s", p.isUsed() ? "t"
							: "f", p.isRequired() ? "t" : "f", String.format(
							"<%s>", p.getName()), p.getDescription()));
				}
			} catch (InstantiationException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}

		} else if (StaticHelper.isSubClassOf(pluginClass,
				AbstractPDGFRandom.class)) {
			log.info("No further information available for Random Plugins");
		}

	}
}
