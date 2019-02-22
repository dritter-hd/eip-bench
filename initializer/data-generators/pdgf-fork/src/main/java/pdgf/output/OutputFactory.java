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
package pdgf.output;

import org.w3c.dom.Node;

import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Output;
import pdgf.util.StaticHelper;

/**
 * Get Output Class for name
 * 
 * @author Michael Frank
 * @version 1.0 18.11.2009
 */
public class OutputFactory {
	private static final OutputFactory of = new OutputFactory();
	private static final String superClass = Output.class.getName();

	/**
	 * private constructor for singelton pattern
	 */
	private OutputFactory() {

	}

	/**
	 * Get Output Class name from a given node
	 * 
	 * @param node
	 *            &lt;Output> node containing the classname in the "name"
	 *            attribute
	 * @param p
	 *            needed for correct error messages
	 * @return a Subclass of Output
	 * @throws XmlException
	 */
	public Output getRowOutput(Node node, Project p) throws XmlException {
		Output ro = null;
		try {
			ro = getRowOutput(StaticHelper.getNodeNameAttr(node, p));

		} catch (ClassNotFoundException e) {
			throw new XmlException(p.getNodeInfo() + e.getMessage());
		}
		ro.setParent(p);
		ro.parseConfig(node);
		return ro;
	}

	/**
	 * Returns the desired Random class. Must be a sub class of java.util.Random
	 * 
	 * @param className
	 * @return the Random class
	 * @throws ClassNotFoundException
	 */
	public Output getRowOutput(String className) throws ClassNotFoundException {

		if (className == null | className.isEmpty()) {
			throw new ClassNotFoundException(
					" OutputFactory can not load class because class name was empty");
		}

		ClassLoader classLoader = OutputFactory.class.getClassLoader();

		Class<?> aclass = null;
		try {
			aclass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			// if no package names before class name, try searching in default
			// package
			if (!className.contains(".")) {
				return getRowOutput(this.getClass().getPackage().getName()
						+ '.' + className);
			} else {
				throw new ClassNotFoundException(" Output Class: " + className
						+ " was not found.");
			}
		}

		boolean isSubClass = false;

		// check if class is a subclass the class specified in superClass
		Class subclass = aclass;
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
			throw new ClassNotFoundException(" Output class " + aclass
					+ "  must be a sub class of " + superClass);
		}
		try {
			return (Output) aclass.newInstance();
		} catch (InstantiationException e) {
			throw new ClassNotFoundException(" Output class " + aclass
					+ " could not be instantiated. " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException(" Output class " + aclass
					+ " caused an IllegalAccessException. " + e.getMessage());
		}

	}

	/**
	 * Singelton Instance of this Factory
	 * 
	 * @return Instance of this Factory
	 */
	public static OutputFactory instance() {
		return of;
	}

}
