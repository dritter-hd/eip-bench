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
package pdgf.util.random;

import org.w3c.dom.Node;

import pdgf.core.Element;
import pdgf.core.exceptions.XmlException;
import pdgf.generator.GeneratorFactory;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.util.StaticHelper;

/**
 * Get Random Class for name
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public class RandomFactory {
	private static final RandomFactory rf = new RandomFactory();
	private static final String superClass = AbstractPDGFRandom.class.getName();

	/**
	 * private constructor for singelton pattern
	 */
	private RandomFactory() {

	}

	/**
	 * Returns the desired Random class. Must be a sub class of
	 * AbstractDmuddgRandom
	 * 
	 * @param className
	 * 
	 * @return the Random class
	 * @throws ClassNotFoundException
	 */
	public AbstractPDGFRandom getRNGClass(Node generatorNode, Element field)
			throws XmlException {
		AbstractPDGFRandom g = null;
		try {
			g = getRNGClass(StaticHelper.getNodeNameAttr(generatorNode, field));

		} catch (ClassNotFoundException e) {
			throw new XmlException(field.getNodeInfo() + e.getMessage());
		}

		return g;

	}

	/**
	 * Returns the desired Random class. Must be a sub class of
	 * AbstractDmuddgRandom
	 * 
	 * @param className
	 * @return the Random class
	 * @throws ClassNotFoundException
	 */
	public AbstractPDGFRandom getRNGClass(String className)
			throws ClassNotFoundException {

		if (className == null | className.isEmpty()) {
			throw new ClassNotFoundException(
					" RandomFactory can not load class becaus class name was empty");
		}

		ClassLoader classLoader = GeneratorFactory.class.getClassLoader();

		// System.out.println("className " +className);
		// System.out.println("classLoader.getResource(className);: "
		// +classLoader.getResource(className));
		// System.out.println("classLoader.getSystemResource(className);: "
		// +classLoader.getSystemResource(className));
		// System.out.println("classLoader.getResource(className.replace);: "
		// +classLoader.getResource(className.replace('.', '/')));
		// System.out.println("classLoader.getSystemResource(className.replace);: "
		// +classLoader.getSystemResource(className.replace('.', '/')));
		// System.out.println("Classpath: " +
		// System.getProperty("java.class.path"));

		Class<?> randomClass = null;
		try {
			// andomClass = classLoader.loadClass(className);
			randomClass = Class.forName(className);
			// Class.forName(className).getConstructor().newInstance();
		} catch (ClassNotFoundException e) {
			// if no package names before class name, try searching in default
			// package
			if (!className.contains(".")) {
				// System.out.println(className + " not found. Trying: "+
				// this.getClass().getPackage().getName() + '.'+ className);
				return getRNGClass(this.getClass().getPackage().getName() + '.'
						+ className);
			} else {
				throw new ClassNotFoundException(" Random Class: " + className
						+ " was not found.");
			}
		}

		boolean isSubClass = false;

		// check if class is a subclass the class specified in superClass
		Class subclass = randomClass;
		Class superclass = subclass.getSuperclass();
		while (superclass != null) {
			// System.out.println("superclass: " + superclass.getName());
			if (superclass.getName().equals(superClass)) {
				isSubClass = true;
				break;
			}
			subclass = superclass;
			superclass = subclass.getSuperclass();
		}

		if (!isSubClass) {
			throw new ClassNotFoundException(" Random class " + randomClass
					+ " must be a sub class of " + superClass);
		}
		try {
			return (AbstractPDGFRandom) randomClass.newInstance();
		} catch (InstantiationException e) {
			throw new ClassNotFoundException(" Random class " + className
					+ " could not be instantiated. " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException(" Random class " + className
					+ " caused an IllegalAccessException. " + e.getMessage());
		}

	}

	/**
	 * Singelton Instance of this Factory
	 * 
	 * @return Instance of this Factory
	 */
	public static RandomFactory instance() {
		return rf;
	}

}
