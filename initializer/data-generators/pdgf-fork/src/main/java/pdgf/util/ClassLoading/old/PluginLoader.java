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
package pdgf.util.ClassLoading.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class PluginLoader {
	private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

	public static ArrayList<File> loadPlugins(String path)
			throws FileNotFoundException {

		if (check(path)) {
			File filePath = new File(path);
			ArrayList<File> addedPlugins = new ArrayList<File>();
			load(filePath, addedPlugins);
			return addedPlugins;
		} else {
			throw new FileNotFoundException("Dir or file\"" + path
					+ "\" does not exist");
		}

	}

	private static void load(File file, ArrayList<File> addedPlugins) {

		if (file.isFile()
				&& file.canRead()
				&& (file.getName().endsWith(".jar") || file.getName().endsWith(
						".class"))) {
			log.debug("Adding File: " + file.getName() + " to classpath");
			ClassPathHacker.addFile(file);
			addedPlugins.add(file);
		} else if (file.isDirectory()) {
			File[] dir = file.listFiles();
			for (File newfile : dir) {

				load(newfile, addedPlugins);// recursive search for files in
											// subdir
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
