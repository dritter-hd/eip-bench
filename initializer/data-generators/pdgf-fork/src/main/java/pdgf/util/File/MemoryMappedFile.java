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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Encapsulates a file reader providing direct random access to the lines of a
 * file. In this case the whole file content is memory mapped into a array of
 * char [] for each line
 * 
 * @author Michael Frank
 * @version 1.0 08.10.2009
 * 
 */
public class MemoryMappedFile extends LineAccessFile {

	private char[][] lines = null;

	public MemoryMappedFile(File fileName) throws FileNotFoundException {
		super(fileName);
		readFile();

	}

	@Override
	public char[] getLine(long lineNo) {
		if (lineNo > this.getLineCount()) {
			throw new IllegalArgumentException("Linnumber to big. Max: "
					+ this.getLineCount());
		}
		return lines[(int) lineNo];
	}

	@Override
	public long getLineCount() {
		return lines.length;
	}

	// FIXME! replace this by a better version
	private void readFile() throws FileNotFoundException {
		ArrayList<char[]> buffer = new ArrayList<char[]>();
		BufferedReader in = null;

		in = new BufferedReader(new FileReader(this.getFile()));
		String tmp = null;
		try {
			while ((tmp = in.readLine()) != null) {
				buffer.add(tmp.toCharArray()); // FIXME! replace this by a
				// faster version
			}
		} catch (IOException e) {
			throw new FileNotFoundException("Failed to read from file: "
					+ e.getMessage());
		}
		lines = new char[buffer.size()][];

		for (int i = 0; i < buffer.size(); i++) {
			lines[i] = buffer.get(i);
		}

	}

}
