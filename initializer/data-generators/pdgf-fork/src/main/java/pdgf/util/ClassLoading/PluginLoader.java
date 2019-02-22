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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * Searches within a provided path recursively for .jar and .class files and
 * adds them to the java classpath. Alternative a array of Filetypes to search
 * for and load can be provided.
 * 
 * @author Michael Frank
 * @version 1.1 11.05.2011
 * 
 */
public class PluginLoader {
	private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);
	public static String[] DEFAULT_FILETYPES = { ".jar", ".class" };
	private static final ClassPathHacker cph = new ClassPathHacker();

	/**
	 * searches recursively within the provided path for resources of
	 * DEFAULT_FILETYPES and adds them to the java classpath.
	 * 
	 * @param path
	 *            to search recursively for resources
	 * @return an ArrayList of found and added resources.
	 * @throws FileNotFoundException
	 */
	public static ArrayList<File> loadPlugins(String path)
			throws FileNotFoundException {
		return loadPlugins(path, DEFAULT_FILETYPES);
	}

	public static URLClassLoader getClassLoader() {
		return cph.getClassLoader();
	}

	/**
	 * searches recursively within the provided path for resources and adds them
	 * to the java classpath.<br>
	 * search is done via filename.endWith(filetypes[i])
	 * 
	 * @param filetypes
	 *            a array of file postfixes to search for in provided path.
	 * @param path
	 *            to search recursively for resources
	 * @return an ArrayList of found and added resources.
	 * @throws FileNotFoundException
	 */
	public static ArrayList<File> loadPlugins(String path, String[] filetypes)
			throws FileNotFoundException {

		if (check(path)) {
			File filePath = new File(path);
			ArrayList<File> addedPlugins = new ArrayList<File>();
			load(filePath, addedPlugins, filetypes);

			try {

				// File [] files = new File[addedPlugins.size()];
				cph.addFiles(addedPlugins);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return addedPlugins;
		} else {
			throw new FileNotFoundException("Dir or file\"" + path
					+ "\" does not exist");
		}

	}

	/**
	 * recursive search routine for files in provided dir.
	 * 
	 * @param fileOrDir
	 *            root folder, or sub-folders
	 * @param addedPlugins
	 */
	private static void load(File fileOrDir, ArrayList<File> addedPlugins,
			String[] filetypes) {

		if (fileOrDir.isFile() && fileOrDir.canRead()) {
			String name = fileOrDir.getName();
			for (int i = 0; i < filetypes.length; i++) {
				if (name.endsWith(filetypes[i])) {
					log.debug("Adding File: " + fileOrDir.getName()
							+ " to classpath");
					// ClassPathHacker.addFile(file);
					addedPlugins.add(fileOrDir);
					break;
				}
			}

		} else if (fileOrDir.isDirectory()) {
			File[] dir = fileOrDir.listFiles();
			for (File newfile : dir) {

				load(newfile, addedPlugins, filetypes);// recursive search for
				// files in subdir
			}
		}

	}

	private static boolean check(String path) {
		if (path == null || path.isEmpty()) {
			return false;
		}

		try {
			File temp = new File(path);
			if (temp.exists() && temp.isDirectory()) {
				return true;
			} else if (temp.exists()
					&& temp.isFile()
					&& (temp.getName().endsWith(".jar") || temp.getName()
							.endsWith(".class"))) {
				return true;
			}
			return false;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}
}
