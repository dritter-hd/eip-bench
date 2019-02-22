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
package pdgf.plugin;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import pdgf.core.Element;
import pdgf.core.RowDataDTO;
import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.XmlException;

/**
 * Outputs a generated Table Row to a destination (for example a CSV file or
 * SQL) destination is specified by the implementation
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public abstract class Output extends Element<Element, Project> {

	private static final Logger log = LoggerFactory.getLogger(Output.class);
	// *******************************************************************
	// constructors
	// *******************************************************************
	/**
	 * Empty constructor. dont forget to {@link Output#setParent(Project)} and
	 * init this object with {@link Output#parseConfig(Node)}. Note: set Parent
	 * before parseConfig! or there will be a mess;
	 * @throws XmlException 
	 */
	public Output(String description) throws XmlException {
		super("output", description);
	}

	// protected abstract void configParsers();

	// *******************************************************************
	// functions
	// *******************************************************************

	/**
	 * The preferred way to write row data objects. Depending on the
	 * Implementation maybe no synchronization is required, because we can
	 * handle each thread separately. <br/>
	 * <br/>
	 * Important! RowData is reusable Object provided by an underlying thread.
	 * Therefore you can NOT! store it for later use/caching, because its
	 * contents are altered. You should process and copy the information
	 * contained in RowData immediately. If you really need to
	 * buffer/cache/store it, be sure to copy your needed data or it will be
	 * lost/altered by the thread providing the RowData. writes a Row of Table
	 * values. For example as CSV or database insert statements. Keep in mind
	 * this is a multithreading environment! you are responsible for
	 * synchronizing what is needed. More then one thread may call this method
	 * at once.
	 * 
	 * @param r
	 *            shared and reusable object. contains fields with values to be
	 *            written.
	 * @param threadID
	 *            id of thread calling this method id: [1, threadCount] for
	 *            threadCount see : {@link Output#initialize(int)} <br/>
	 *            if threadID < 1 : unidentified thread! direct call to ->
	 *            {@link Output#write(RowDataDTO)}
	 */
	public abstract void write(RowDataDTO r, int threadID) throws IOException;

	/**
	 * Uses {@link Output#write(RowDataDTO r, int threadID)} is strongly
	 * recommended, because it allows the underlying implementation to react
	 * more precisely to the calling threads write request. Depending on the
	 * Implementation maybe no synchronization is required if
	 * {@link Output#write(RowDataDTO r, int threadID)} is used. This is NOT the
	 * case for this method!.<br/>
	 * <br/>
	 * Important! RowData is reusable Object provided by an underlying thread.
	 * Therefore you can NOT! store it for later use/caching, because its
	 * contents are altered. You should process and copy the information
	 * contained in RowData immediately. If you really need to
	 * buffer/cache/store it, be sure to copy your needed data or it will be
	 * lost/altered by the thread providing the RowData. writes a Row of Table
	 * values. For example as CSV or database insert statements. Keep in mind
	 * this is a multithreading environment! you are responsible for
	 * synchronizing what is needed. More then one thread may call this method
	 * at once.
	 * 
	 * @param r
	 *            shared and reusable object. contains fields with values to be
	 *            written.
	 */
	// public abstract void write(RowDataDTO r) throws IOException;

	/**
	 * Close this output. No new write operations are allowed! Keep in mind this
	 * is a multithreading environment! you are responsible for synchronizing
	 * what is needed
	 * 
	 * @throws IOException
	 * 
	 */
	public abstract void close() throws IOException;

	/**
	 * If caching is used, flushes all cached data.
	 * 
	 * @throws IOException
	 */
	public abstract void flush() throws IOException;

	/**
	 * If caching is used, flushes only the cache of the calling thread if per
	 * thread caching is used.
	 * 
	 * @throws IOException
	 */
	public abstract void flush(int workerID) throws IOException;

}
