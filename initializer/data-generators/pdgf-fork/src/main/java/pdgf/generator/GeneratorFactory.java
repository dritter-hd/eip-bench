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
package pdgf.generator;

import org.w3c.dom.Node;

import pdgf.core.dbSchema.Field;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Generator;
import pdgf.util.StaticHelper;

/**
 * Get Generator Class for name
 * 
 * @author Michael Frank
 * @version 1.0 18.11.2009
 */
public class GeneratorFactory {

	private static final GeneratorFactory instance = new GeneratorFactory();
	private static final String superClass = Generator.class.getName();

	/**
	 * private Constructor for Singelton Pattern
	 */
	private GeneratorFactory() {

	}

	public static GeneratorFactory instance() {
		return instance;
	}

	/**
	 * Get Generator Class name from a given node
	 * 
	 * @param generatorNode
	 *            &lt;Generator> node containing the classname in the "name"
	 *            attribute
	 * @param field
	 *            needed for correct error messages
	 * @return a Subclass of Generator
	 * @throws XmlException
	 */
	public Generator getGenerator(Node generatorNode, Field field)
			throws XmlException {

		Generator g = null;
		try {
			g = getGenerator(StaticHelper.getNodeNameAttr(generatorNode, field));

		} catch (ClassNotFoundException e) {
			throw new XmlException(field.getNodeInfo() + e.getMessage());
		}
		g.setParent(field);
		g.parseConfig(generatorNode);
		return g;
	}

	/**
	 * Class for name
	 * 
	 * @param className
	 *            Name of distribution class
	 * @return a Subclass of Distribution
	 * @throws ClassNotFoundException
	 */
	public Generator getGenerator(String className)
			throws ClassNotFoundException {

		if (className == null | className.isEmpty()) {
			throw new ClassNotFoundException(
					" GeneratorFactory can not load class because class name was empty");
		}

		ClassLoader classLoader = GeneratorFactory.class.getClassLoader();

		Class<?> generatorClass = null;

		try {
			generatorClass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			// if no package names before class name, try searching in default
			// package
			if (!className.contains(".")) {
				return getGenerator(this.getClass().getPackage().getName()
						+ '.' + className);
			} else {
				throw new ClassNotFoundException(" Generator Class "
						+ className + " was not found");
			}
		}

		// check if class is a subclass the class specified in superClass
		boolean isSubClass = false;
		Class subclass = generatorClass;
		Class superclass = subclass.getSuperclass();

		while (superclass != null) {
			if (superclass.getName().equals(superClass)) {
				isSubClass = true;
				break;
			}
			subclass = superclass;
			superclass = subclass.getSuperclass();
		}

		if (!isSubClass) {
			throw new ClassNotFoundException(" Generator " + className
					+ " must be a sub class of " + superClass);
		}

		try {
			return (Generator) generatorClass.newInstance();
		} catch (InstantiationException e) {
			throw new ClassNotFoundException(" Generator Class " + className
					+ " could not be instantiated. " + e.getMessage());

		} catch (NoClassDefFoundError e) {
			throw new ClassNotFoundException(" Generator Class " + className
					+ " could not be instantiated. " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException(" Generator Class " + className
					+ " caused an IllegalAccessException. " + e.getMessage());
		}

	}

}
