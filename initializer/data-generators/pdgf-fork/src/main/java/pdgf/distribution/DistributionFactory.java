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
package pdgf.distribution;

import org.w3c.dom.Node;

import pdgf.core.exceptions.XmlException;
import pdgf.generator.GeneratorFactory;
import pdgf.plugin.Distribution;
import pdgf.plugin.Generator;
import pdgf.util.StaticHelper;

/**
 * Get Distribution Class for Name
 * 
 * @author Michael Frank
 * @version 1.0 18.11.2009
 */
public class DistributionFactory {
	private static final DistributionFactory instance = new DistributionFactory();
	private static final String superClass = Distribution.class.getName();

	/**
	 * private Constructor for Singelton Pattern
	 */
	private DistributionFactory() {

	}

	public static DistributionFactory instance() {
		return instance;
	}

	/**
	 * Get Distribution Class name from a given node
	 * 
	 * @param node
	 *            &lt;distribution> node containing the classname in the "name"
	 *            attribute
	 * @param element
	 *            needed for correct error messages
	 * @return a Subclass of Distribution
	 * @throws XmlException
	 */
	public Distribution getDistribution(Node node, Generator element)
			throws XmlException {
		Distribution d = null;
		try {
			d = getDistribution(StaticHelper.getNodeNameAttr(node, element));

		} catch (ClassNotFoundException e) {
			throw new XmlException(element.getNodeInfo() + e.getMessage());
		}
		d.setParent(element);
		d.parseConfig(node);
		return d;
	}

	/**
	 * Class for name
	 * 
	 * @param className
	 *            Name of distribution class
	 * @return a Subclass of Distribution
	 * @throws ClassNotFoundException
	 */
	public Distribution getDistribution(String className)
			throws ClassNotFoundException {
		if (className == null | className.isEmpty()) {
			throw new ClassNotFoundException(
					" DistrubutionFactory can not load class because class name was empty");
		}
		ClassLoader classLoader = GeneratorFactory.class.getClassLoader();

		Class<?> distributionClass = null;

		try {
			distributionClass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			/*
			 * if no package name before class name, try searching in default
			 * package: dmuddg.distribution
			 */
			if (!className.contains(".")) {
				return getDistribution(this.getClass().getPackage().getName()
						+ '.' + className);
			} else {
				throw new ClassNotFoundException(" Distrubution Class: "
						+ className + " was not found.");
			}
		}

		// check if class is a subclass the class specified in superClass
		boolean isSubClass = false;
		Class subclass = distributionClass;
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
			throw new ClassNotFoundException(" Distrubution " + className
					+ " must be a sub class (extends) of " + superClass);
		}

		try {
			return (Distribution) distributionClass.newInstance();
		} catch (InstantiationException e) {
			throw new ClassNotFoundException(" Distrubution Class " + className
					+ " could not be instantiated. " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException(" Distrubution Class " + className
					+ " caused an IllegalAccessException. " + e.getMessage());
		}

	}
}
