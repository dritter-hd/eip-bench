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
 * Containts constants used in this application
 * 
 * @author Michael Frank
 * @version 1.0 08.10.2009
 * 
 */
public class Constants {
	public static final String ERROR = "ERROR! ";
	public static final int INT_NOT_SET = -1;
	public static final long LONG_NOT_SET = -1;
	public static final double DOUBLE_NOT_SET = Double.NaN;
	public static final String TEXT_NODE = "#text";
	public static final String COMMENT_NODE = "#comment";
	public static final String DEFAULT = "default";
	public static final int MAX_CACHABLE_FILE_SIZE = 2 * 1024 * 1024; // 1mb
	public static final long GENERATION_PROGRESS_UPDATE_INTERVALL = 1 * 1000; // 10s
	public static final String DEFAULT_CONFIG_FILE_DIR = "config";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final long ONE_DAY_IN_ms = 24 * 60 * 60 * 1000;
	public static final int NULL_CHANCE_PRECISION = 100000;
	public static final String NON_BUILD_VERSION = "1.1 Experimental";

}
