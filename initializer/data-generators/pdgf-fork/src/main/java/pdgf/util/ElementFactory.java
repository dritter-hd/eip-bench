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
package pdgf.util;

import pdgf.core.Element;
import pdgf.plugin.AbstractPDGFRandom;

/**
 * Get Output Class for name
 * 
 * @author Michael Frank
 * @version 1.0 09.05.2010
 */
public class ElementFactory {
	private static final ElementFactory ef = new ElementFactory();
	private static final String[] supportedSuperClasses = {
			Element.class.getName(), AbstractPDGFRandom.class.getName() };

	/**
	 * private constructor for singelton pattern
	 */
	private ElementFactory() {

	}

	/**
	 * Returns the desired Random class. Must be a sub class of java.util.Random
	 * 
	 * @param className
	 * @return the Random class
	 * @throws ClassNotFoundException
	 */
	public Object getElement(String className) throws ClassNotFoundException {

		if (className == null | className.isEmpty()) {
			throw new ClassNotFoundException(
					" ElementFactory can not load class because class name was empty");
		}

		ClassLoader classLoader = ElementFactory.class.getClassLoader();

		Class<?> aclass = null;
		try {
			aclass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new ClassNotFoundException(" Element Class: " + className
					+ " was not found. msg:" + e.getMessage());

		} catch (NoClassDefFoundError e) {
			throw new ClassNotFoundException(" Element Class: " + className
					+ " was not found.NoClassDef " + e.getMessage());
		}

		boolean isSubClass = false;

		// check if class is a subclass of the class specified in superClass
		for (int i = 0; i < supportedSuperClasses.length && !isSubClass; i++) {
			isSubClass = checkIfSubclass(aclass, supportedSuperClasses[i]);

		}

		if (!isSubClass) {
			StringBuilder sb = new StringBuilder(" Class ");
			sb.append(aclass);
			sb.append("  must be a sub class of ");
			for (int i = 0; i < supportedSuperClasses.length; i++) {
				sb.append(supportedSuperClasses[i]);
				// add a "or" separator except for last element
				if (i != supportedSuperClasses.length - 1)
					sb.append(" or ");
			}

			throw new ClassNotFoundException(sb.toString());
		}
		try {
			return aclass.newInstance();
		} catch (InstantiationException e) {
			throw new ClassNotFoundException(" Class " + aclass
					+ " could not be instantiated. " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException(" Class " + aclass
					+ " caused an IllegalAccessException. " + e.getMessage());
		}

	}

	public static String[] getSupportedsuperclasses() {
		return supportedSuperClasses;
	}

	private boolean checkIfSubclass(Class<?> aclass, String superClazzName) {
		boolean isSubClass = false;
		Class subclass = aclass;
		Class superclass = subclass.getSuperclass();
		while (superclass != null) {
			if (superclass.getName().equals(superClazzName)) {
				isSubClass = true;
				break;
			}
			subclass = superclass;
			superclass = subclass.getSuperclass();
		}
		return isSubClass;
	}

	/**
	 * Singelton Instance of this Factory
	 * 
	 * @return Instance of this Factory
	 */
	public static ElementFactory instance() {
		return ef;
	}

}
