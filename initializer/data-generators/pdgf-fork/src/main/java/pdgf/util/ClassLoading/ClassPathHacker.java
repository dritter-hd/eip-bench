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
package pdgf.util.ClassLoading;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;

/**
 * The ClassPathHacker gives you the ability to add files (like .jar and .class
 * files) during runtime to the java Classpath. This is quite useful for
 * dynamically loading plugins, lazy initialization strategies and solving other
 * annoying issues with javas handling of class loading and finding resources
 * within jars. For a single file, its best to use the static methods. For
 * processing more files with this class it is wise to create an instance of it,
 * as Java Reflection can be quite slow.
 * 
 * @author Michael Frank
 * @version 1.1 11.05.2011
 * 
 */
public class ClassPathHacker {

	private static final Class[] params = new Class[] { URL.class };
	private URLClassLoader loader = null;
	private Method addClass;

	/**
	 * New Classpath hacker for batch loading multiple files. If only a single
	 * file should be added, use the static add methods.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public ClassPathHacker() {
		loader = getClassLoader();

		try {
			addClass = URLClassLoader.class.getDeclaredMethod("addURL", params);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addClass.setAccessible(true);

	}

	public static URLClassLoader getClassLoader() {
		return (URLClassLoader) ClassLoader.getSystemClassLoader();
	}

	/**
	 * add provided files/resources (like .class or .jar files) to classpath
	 * 
	 * @param files
	 * @throws IllegalArgumentException
	 * @throws MalformedURLException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void addFiles(List<File> files) throws IllegalArgumentException,
			MalformedURLException, IllegalAccessException,
			InvocationTargetException {
		URL[] urls = new URL[files.size()];
		int i = 0;
		for (File file : files) {
			urls[i++] = file.toURL();
		}
		addURLs(urls);
	}

	/**
	 * add provided files/resources (like .class or .jar files) to classpath
	 * 
	 * @param files
	 * @throws IllegalArgumentException
	 * @throws MalformedURLException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void addFiles(String[] files) throws IllegalArgumentException,
			MalformedURLException, IllegalAccessException,
			InvocationTargetException {
		for (int i = 0; i < files.length; i++) {
			addURL_(new File(files[i]).toURI().toURL());
		}
	}

	/**
	 * add provided files/resources (like .class or .jar files) to classpath
	 * 
	 * @param files
	 * @throws IllegalArgumentException
	 * @throws MalformedURLException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void addFiles(File[] files) throws IllegalArgumentException,
			MalformedURLException, IllegalAccessException,
			InvocationTargetException {
		URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			urls[i] = files[i].toURI().toURL();
		}
		addURLs(urls);
	}

	/**
	 * add provided files/resources (like .class or .jar files) to classpath
	 * 
	 * @param url
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void addURLs(URL[] url) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		HashMap<URL, URL> urlLookup = getUrlLookupMap();

		for (int i = 0; i < url.length; i++) {
			if (!urlLookup.containsKey(url[i])) {
				// System.out.println("new Classpath location added: " +
				// url[i].toString());
				addClass.invoke(loader, new Object[] { url[i] });
			}
		}
	}

	/**
	 * add provided file/resource (like .class or .jar files) to classpath (this
	 * method is used by the other add methods.
	 * 
	 * @param url
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void addURL_(URL url) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		HashMap<URL, URL> urlLookup = getUrlLookupMap();

		if (!urlLookup.containsKey(url)) {
			addClass.invoke(loader, new Object[] { url });
		}
	}

	/**
	 * Adds a single file (.class, .jar etc.) to the java classpath. For more
	 * files, do not use this static interface. Instead create a instance of
	 * this class to do batch processing.
	 * 
	 * @param url
	 *            of file to add to classpath
	 */
	public static void addURL(URL url) {

		URLClassLoader loader = getClassLoader();

		HashMap<URL, URL> urlLookup = getUrlLookupMap(loader);

		if (!urlLookup.containsKey(url)) {
			try {
				Method method = URLClassLoader.class.getDeclaredMethod(
						"addURL", params);
				method.setAccessible(true);
				method.invoke(loader, new Object[] { url });

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds a single file (.class, .jar etc.) to the java classpath. For more
	 * files, do not use this static interface. Instead create a instance of
	 * this class to do batch processing.
	 * 
	 * @param filename
	 *            and path to add to classpath
	 */
	public static void addFile(String filename) {
		File f = new File(filename);
		addFile(f);
	}

	/**
	 * Adds a single file (.class, .jar etc.) to the java classpath. For more
	 * files, do not use this static interface. Instead create a instance of
	 * this class to do batch processing.
	 * 
	 * @param f
	 *            file to add to classpath
	 */
	public static void addFile(File f) {
		try {
			addURL(f.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static InputStream getResourceAsStream(String name) {
		URLClassLoader loader = getClassLoader();
		return loader.getResourceAsStream(name);
	}

	public InputStream getResourceAsStream_(String name) {
		return loader.getResourceAsStream(name);
	}

	public static URL getResource(String name) {
		URLClassLoader loader = getClassLoader();
		return loader.getResource(name);
	}

	public URL getResource_(String name) {

		return loader.getResource(name);
	}

	public HashMap<URL, URL> getUrlLookupMap() {
		return getUrlLookupMap(loader);

	}

	public static HashMap<URL, URL> getUrlLookupMap(URLClassLoader loader) {
		URL[] existingurls = loader.getURLs();
		HashMap<URL, URL> urlLookup = new HashMap<URL, URL>(existingurls.length);
		for (int i = 0; i < existingurls.length; i++) {
			urlLookup.put(existingurls[i], existingurls[i]);
		}
		return urlLookup;
	}
}
