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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import pdgf.Controller;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.util.ClassLoading.PluginLoader;

public class LoadPluginsAction extends Action {

	private ArrayList<String> pluginDirs = Controller.getInstance()
			.getPluginDirs();

	public LoadPluginsAction() {
		super(
				"findPlugins",
				"<path to dir/file>(optional)",
				"searches in plugin and lib dir for new plugins. If path parameter is specified: search recursively in given path or load specified file",
				0, 1);
	}

	@Override
	public void execute(String[] tokens) throws FileNotFoundException,
			InvalidArgumentException {
		checkParamQuantity(tokens);
		StringBuilder buf = new StringBuilder("(re)loaded plug-ins in: ");
		for (String path : pluginDirs) {
			PluginLoader.loadPlugins(path);

			buf.append(path);
			buf.append(File.separatorChar);
			buf.append("; ");
		}

		// tokens was specified, so this method was not called by constructor so
		// give user a feedback
		if (tokens != null) {
			log.info(buf.toString());
		}

		if (tokens != null && tokens.length > 1 && !tokens[1].isEmpty()) {
			ArrayList<File> foundPlugins;
			foundPlugins = PluginLoader.loadPlugins(tokens[1]);
			// if without error add to pluginDirs list
			pluginDirs.add(tokens[1]);
			buf.setLength(0); // reset
			buf.append("Added the following classes and jars to class path: ");
			for (File file : foundPlugins) {
				buf.append(file.getPath());
				buf.append("\n");
			}
			log.info(buf.toString());
		}
	}
}
