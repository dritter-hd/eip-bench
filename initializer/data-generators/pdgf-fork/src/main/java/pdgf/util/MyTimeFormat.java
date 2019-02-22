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

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public final class MyTimeFormat {

	/* instance variables */

	/**
	 * holds the systematic of how to display the time, for example mm:ss
	 */
	private String format;

	/* methods */

	/**
	 * constructor. sets the systematic of how to display the time.
	 * 
	 * @param format
	 *            this String encodes how to display the time. it can be any
	 *            String, where some substrings get replaced by their
	 *            'meanings'.<br>
	 *            hh -> hours<br>
	 *            mm -> minutes<br>
	 *            ss -> seconds<br>
	 *            SSS -> milliseconds<br>
	 */
	public MyTimeFormat(String format) {
		this.format = format;
	}

	/**
	 * formats the given time according to the MyTime object.
	 * 
	 * @param timeInMilliseconds
	 *            the time to format.
	 * @return the formatted time.
	 */
	public String format(long timeInMilliseconds) {

		int milliseconds = (int) (timeInMilliseconds % 1000);
		timeInMilliseconds /= 1000;

		byte seconds = (byte) (timeInMilliseconds % 60);
		timeInMilliseconds /= 60;

		byte minutes = (byte) (timeInMilliseconds % 60);
		timeInMilliseconds /= 60;

		int hours = (int) (timeInMilliseconds);

		String h = hours < 10 ? "0" + hours : "" + hours;
		String m = minutes < 10 ? "0" + minutes : "" + minutes;
		String s = seconds < 10 ? "0" + seconds : "" + seconds;
		String ms;
		if (milliseconds < 10)
			ms = "00" + milliseconds;
		else if (milliseconds < 100)
			ms = "0" + milliseconds;
		else
			ms = "" + milliseconds;

		return format.replace("hh", h).replace("mm", m).replace("ss", s)
				.replace("SSS", ms);
	}

}