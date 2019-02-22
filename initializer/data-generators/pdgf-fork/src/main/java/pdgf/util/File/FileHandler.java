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
package pdgf.util.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdgf.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Michael Frank
 * @version 1.0 14.12.2009
 */
public class FileHandler {
	private static final Logger log = LoggerFactory.getLogger(FileHandler.class);
	private static FileHandler instance = new FileHandler(); // init singelton
	// instance

	private int fileSizeThreshold = Constants.MAX_CACHABLE_FILE_SIZE; // 2MiB

	// cache for files
	private Map<File, LineAccessFile> files;

	/**
	 * Private constructor for singelton
	 */
	private FileHandler() {
		files = new ConcurrentHashMap<File, LineAccessFile>();
	}

	/**
	 * Get singelton instance of FileHandler
	 * 
	 * @return
	 */
	public static FileHandler instance() {
		return instance;
	}

	public LineAccessFile getLineAccessFile(File file) throws IOException {
		log.debug("Try to get line access for file: {}", file);

		LineAccessFile laf = files.get(file);
		if (laf == null) {
			return addFile(file);
		} else {
			return laf;
		}
	}

	/**
	 * Get desired line of a file
	 * 
	 * @param file
	 * @param line
	 * @return null if file does not exist.
	 */
	public char[] getLine(File file, int line) {
		LineAccessFile laf = files.get(file);
		if (laf == null) {
			// should not happen //FIXME! check this
			return null;
		} else {
			return laf.getLine(line);
		}
	}

	/**
	 * get total number of lines of a file
	 * 
	 * @param filename
	 * @return -1 if file does not exist
	 */
	public long getLineCount(File filename) {
		LineAccessFile laf = files.get(filename);
		if (laf == null) {
			// should not happen! initialize before use!
			return -1;
		} else {
			return laf.getLineCount();
		}
	}

	private LineAccessFile addFile(File f) throws IOException {
		LineAccessFile laf;

		if (checkFile(f)) {

			// TODO! activate this when BinarySearchFile is working correctly
			/*
			 * if (f.length() > fileSizeThreshold) { laf = new
			 * BinarySearchFile(f);
			 * 
			 * } else { laf = new MemoryMappedFile(f); }
			 */

			laf = new MemoryMappedFile(f);
			files.put(f, laf);
			return laf;
		} else {
			return null;
		}

	}

	private boolean checkFile(File f) throws IOException {

		if (!f.exists() || !f.isFile()) {
			throw new IOException("File " + f.getName()
					+ " was not found or is not a file!");
		} else if (!f.canRead()) {
			throw new IOException("No read permission for file "
					+ f.getAbsolutePath());
		} else {
			return true;
		}
	}

}
