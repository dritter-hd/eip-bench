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

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Encapsulates a file reader providing direct random access to the lines of a
 * file
 * 
 * @author Michael Frank
 * @version 1.0 08.10.2009
 * 
 */
public abstract class LineAccessFile {

	private File file = null;

	public LineAccessFile(File f) throws FileNotFoundException {
		this.file = f;
		checkFile(file);
	}

	/**
	 * Returns the file object this LineAccessFile encapsulates
	 * 
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Filename of the file this LineAccessFile provides access to.
	 * 
	 * @return
	 */
	public String getFileName() {
		return file.getName();
	}

	/**
	 * Checks if a File exists, is a file, is readable and not empty
	 * 
	 * @param f
	 *            file to check
	 * @return true if File exists, is a file, is readable and not empty
	 * @throws FileNotFoundException
	 */
	protected static boolean checkFile(File f) throws FileNotFoundException {

		if (!f.exists()) {
			throw new FileNotFoundException("File \"" + f.getAbsolutePath()
					+ "\" does not exist");

		} else if (!f.isFile()) {
			throw new FileNotFoundException("File \"" + f.getAbsolutePath()
					+ "\" is not a file");
		} else if (!f.canRead()) {
			throw new FileNotFoundException("File \"" + f.getAbsolutePath()
					+ "\" no read permission");
		} else if (f.length() == 0) {
			throw new FileNotFoundException("File \"" + f.getAbsolutePath()
					+ "\" is empty");
		} else {
			return true;
		}

	}

	/**
	 * Returns the Line at lineNo with lineNo element [0,getLineCount()[
	 * 
	 * @param lineNo
	 *            line to return
	 * @return line at lineNo
	 */
	public abstract char[] getLine(long lineNo);

	/**
	 * Number of lines in this File
	 * 
	 * @return Number of lines in this File
	 */
	public abstract long getLineCount();

}
