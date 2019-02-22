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
package pdgf.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import pdgf.core.Element;
import pdgf.core.Parser;
import pdgf.core.RowDataDTO;
import pdgf.core.dbSchema.Table;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Output;
import pdgf.util.EncodingCharLineCache2;
import pdgf.util.StaticHelper;

/**
 * Writes the Generated Lines of a Database table into a comma separated file.
 * The file name consist of the table name followed by the nodeId<br/>
 * <br/>
 * Separate, reusable, self resizing line buffer per thread.
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public final class CSVRowOutput extends Output implements FileOutput {
	private static final Logger log = LoggerFactory.getLogger(CSVRowOutput.class);
	public static final String NODE_PARSER_outputDir = "outputDir";
	public static final String NODE_PARSER_delimiter = "delimiter";
	public static final String NODE_PARSER_bufferSize = "bufferSize";
	public static final String NODE_PARSER_lineBufferScale = "lineBufferScale";
	public static final String NODE_PARSER_fileEnding = "fileEnding";
	public static final String NODE_PARSER_encodeDirect = "encodeDirect";
	public static final String NODE_PARSER_compressionLevel = "compressionLevel";
	public static final String NODE_PARSER_charset = "charset";

	private Object writers_Mutex = new Object();

	// between file and fileEnding
	private static final char FILE_ENDING_SEPERATOR = '.';
	private static final char TEXT_QUOTE = '"';
	private static final char NEW_LINE = '\n';

	// one writer per Table
	private FileOutputStream[] writers = null;

	// parameters and their default values, may be overridden by config file
	private int bufsize = 1024 * 4; // so 4Kb
	private int lineBufScale = 10; // so 40kb of data is cached before write
	private String fileEnding = ".csv";
	private char delimiter = ';';
	private byte[] encodedDelimiter;
	private byte[] encodedQuote;
	private byte[] encoded_NEW_LINE;
	private String outputDir = "";
	private boolean encodeDirect = false;
	private Charset charset = Charset.forName("UTF-8"); // default charset
	private ArrayList<File> files = new ArrayList<File>();
	// custom line buffer
	private final static int lineSizeMin = 16;

	// one buf per thread
	private int[] lastTableIDofThread;
	private EncodingCharLineCache2[] threadLineBuffers;
	private int threadCount;

	private boolean closed = true;

	public CSVRowOutput() throws XmlException {
		super(
				"Takes a RowDataDTO (containing a generated row) and writes this row into a CSV file. This RowOutput does not provide ordering of rows. Rows are written when they arrive form the worker threads. Two or more programm runs may not result in the same output as it conceners ordering of generated rows.");
	}

	@Override
	public synchronized void close() throws IOException {
		closed = true;
		synchronized (writers_Mutex) {
			flush();
			for (OutputStream writer : writers) {
				writer.close();
			}
		}
	}

	@Override
	public synchronized void flush() throws IOException {

		synchronized (writers_Mutex) {
			if (threadLineBuffers != null) {
				for (int i = 0; i < threadLineBuffers.length; i++) {
					if (threadLineBuffers[i] != null) {
						threadLineBuffers[i].flush();
					}
				}

			}
			for (OutputStream writer : writers) {
				if (writers != null) {
					writer.flush();
					writer.close();
				}

			}
		}
	}

	@Override
	public synchronized void flush(int workerID) throws IOException {
		if (threadLineBuffers != null && workerID < threadLineBuffers.length
				&& threadLineBuffers[workerID] != null) {
			threadLineBuffers[workerID].flush();
		}

	}

	@Override
	public synchronized void initialize(int workerCount)
			throws ConfigurationException, XmlException {
		super.initialize(workerCount);

		encodedDelimiter = EncodingCharLineCache2.encode(delimiter, charset);
		encodedQuote = EncodingCharLineCache2.encode(TEXT_QUOTE, charset);
		encoded_NEW_LINE = EncodingCharLineCache2.encode(NEW_LINE, charset);
		Table[] tables = this.getParent().getChilds();

		if (tables == null || tables.length < 1) {
			throw new ConfigurationException(
					"Project has no tables! Project Config file not loaded?");

		}

		synchronized (writers_Mutex) {

			if (workerCount < 1) {
				workerCount = 1;
			}

			// one writer per Table
			this.writers = new FileOutputStream[tables.length];

			log.debug("Create now " + writers.length + " writers");

			try {
				// crate writers
				for (int i = 0; i < writers.length; i++) {
					writers[i] = createNewWriter(tables[i].getName());

				}
			} catch (IOException e) {
				log.error("Failed to initialize " + this.getClass().getName()
						+ " cause: " + e.getMessage());
				log.debug("Failed to initialize " + this.getClass().getName()
						+ " cause: " + e.getMessage(), e);
			}

			// init a lineBuffer per thread (worker)
			this.threadCount = workerCount + 1;
			threadLineBuffers = new EncodingCharLineCache2[this.threadCount];
			lastTableIDofThread = new int[this.threadCount];
			Arrays.fill(lastTableIDofThread, -1);

			/*
			 * for (int i = 0; i < threadLineBuffers.length; i++) {
			 * initLinebuffer(i, writers[i]); }
			 */
			closed = false;
		}

	}

	private FileOutputStream createNewWriter(String fileName)
			throws FileNotFoundException, IOException {
		File f = newFile(fileName);
		files.add(f);
		FileOutputStream fos = new FileOutputStream(f);
		// BufferedOutputStream bos = new BufferedOutputStream(fos, bufsize*
		// lineBufScale);
		return fos;
	}

	/**
	 * unsynchronized!
	 * 
	 * @param workerCount
	 * @throws ConfigurationException
	 */
	private void initLinebuffer(int threadId, FileOutputStream fos) {
		threadLineBuffers[threadId] = new EncodingCharLineCache2(charset,
				bufsize * lineBufScale, fos,
				EncodingCharLineCache2.WRITE_TO_CHANNEL);
		threadLineBuffers[threadId].setEncodeDirect(encodeDirect);
	}

	/*
	 * @Override public void write(RowDataDTO r) throws IOException { // fw is
	 * synchronized, so no sync needed here log.debug("write rowData for: " +
	 * r.getParent() + " : " + r.getParent().getElementID()); OutputStream out =
	 * writers[r.getParent().getElementID()];
	 * 
	 * // new linebuf per line; char[] line = new char[lineSize.get()]; int pos
	 * = 0; // position in buffer int newPos; // temporary new pos in buffer if
	 * add operation would be // done boolean needSingleQuote;
	 * 
	 * // FieldValues should never be null! checked by Worker in the generation
	 * // proccess FieldValueDTO[] fieldValues = r.fieldValues; String
	 * stringFieldVal; // string representation of FieldValue int valLen; //
	 * length of stringFieldVal
	 * 
	 * // concatenate FieldsVal.toString in line buf for (int i = 0; i <
	 * fieldValues.length; i++) { // static, so no concurency problem
	 * needSingleQuote = StaticHelper.needSingleQuote(fieldValues[i]
	 * .getType());
	 * 
	 * stringFieldVal = fieldValues[i].getValue().toString(); valLen =
	 * stringFieldVal.length(); newPos = pos + valLen;
	 * 
	 * if (needSingleQuote) { // expand? if (newPos + 2 >= line.length) { line =
	 * expand(line, newPos + 2); }
	 * 
	 * // TEXT_QUOTE only read access and no write access after // initialize()
	 * was called line[pos++] = TEXT_QUOTE;
	 * 
	 * stringFieldVal.getChars(0, valLen, line, pos); pos = pos + valLen;
	 * 
	 * // TEXT_QUOTE only read access and no write access after // initialize()
	 * was called line[pos++] = TEXT_QUOTE; } else { // expand? if (newPos >=
	 * line.length) { line = expand(line, newPos); }
	 * 
	 * stringFieldVal.getChars(0, valLen, line, pos); pos = newPos; }
	 * 
	 * // is last field? if (i == fieldValues.length - 1) { // expand? if (pos +
	 * 1 >= line.length) { line = expand(line, pos + 1); } //
	 * System.out.println(Thread.currentThread().getName() // +
	 * ": lastfield: linelen: " + line.length + " linpos:" // + pos);
	 * line[pos++] = NEW_LINE; } else { // expand? if (pos + 1 >= line.length) {
	 * line = expand(line, pos + 1); } line[pos++] = delimiter; } }
	 * 
	 * out.write(line, 0, pos);
	 * 
	 * if (pos > lineSizeMin) { // lineSize is atomic, pos is local so no
	 * concurrency problem here lineSize.set(pos + pos / 10); } else { //
	 * lineSize is atomic, lineSizeMin is static final read only so no //
	 * concurrency problem her lineSize.set(lineSizeMin); } }
	 */

	@Override
	public void write(RowDataDTO r, int threadID) throws IOException {
		if (closed) {
			throw new IOException(
					"This writer was closed or not initialized before");
		}
		int tableID = r.parent.getElementID();
		/*
		 * if table of a thread changes, reset the threadLinebuffer to standard
		 * size. This is to avoid to big line buffer to be used for a table that
		 * does not need that.
		 */

		if (lastTableIDofThread[threadID] != tableID) {

			// flush line buffer and set writer for next table before clearing
			// it;
			if (threadLineBuffers[threadID] != null) {
				threadLineBuffers[threadID].setOutputStream(writers[tableID]);
			} else {
				initLinebuffer(threadID, writers[tableID]);
			}
			log.debug("re-init linebuffer of thread " + threadID
					+ " for new table");
			lastTableIDofThread[threadID] = tableID;

		}
		/*
		 * be sure to copy all needed information from row data into an output
		 * buffer before returning to the calling thread, because RowData (and
		 * its contents) will be reused by the thread.
		 */

		/*
		 * select filewriter by table Element id; only get operation for , no
		 * synchronization needed
		 */

		writeLine(r, writers[tableID], threadID);

		// after this, all information contained in row data is copied into
		// the
		// bufferdwriter of the OwnFileWriter, so RowData can now be reused
		// by
		// calling thread

	}

	/**
	 * thread safe by design, only local variables, atomics or read only access
	 * to fields.
	 * 
	 * @param data
	 *            data to be written
	 * @param line
	 * @param writers
	 * @throws IOException
	 */
	private void writeLine(RowDataDTO data, OutputStream out, int threadID)
			throws IOException {
		int beforeLastDelimiter = 0;
		Object value;
		String stringFieldVal; // string representation of current FieldValue
		int valLen; // length of stringFieldVal

		// FieldValues should never be null! checked by Worker in the generation
		// Process
		// FieldValueDTO[] fieldValues = data.fieldValues;

		// concatenate FieldsVal.toString in line buf
		for (int i = 0; i < data.fieldValues.length; i++) {
			// static, so no concurrency problem
			value = data.fieldValues[i].getValue();
			// special case value of data is char[]

			if (value == null) {
				if (StaticHelper.needSingleQuote(data.fieldValues[i].getType())) {
					threadLineBuffers[threadID].append(TEXT_QUOTE);

					// maybe write "NULL"??,
					// threadLineBuffers[threadID].append("NULL");

					threadLineBuffers[threadID].append(TEXT_QUOTE);
				}
			} else if (value instanceof char[]) {

				if (StaticHelper.needSingleQuote(data.fieldValues[i].getType())) {
					threadLineBuffers[threadID].append(TEXT_QUOTE);
					threadLineBuffers[threadID].append((char[]) value);

					threadLineBuffers[threadID].append(TEXT_QUOTE);
				} else {
					threadLineBuffers[threadID].append((char[]) value);
				}

			} else {
				stringFieldVal = value.toString();
				if (StaticHelper.needSingleQuote(data.fieldValues[i].getType())) {
					threadLineBuffers[threadID].append(TEXT_QUOTE);
					threadLineBuffers[threadID].append(stringFieldVal);

					threadLineBuffers[threadID].append(TEXT_QUOTE);
				} else {
					threadLineBuffers[threadID].append(stringFieldVal);
				}

			}

			// add delimiter except last field
			if (i != data.fieldValues.length - 1) {
				threadLineBuffers[threadID].append(delimiter);
			}

		}

		// replace last delimiter by newline
		threadLineBuffers[threadID].append(NEW_LINE);

		// maybe flush if buffersize reached
		threadLineBuffers[threadID].flushIfFull();

	}

	private File newFile(String fileName) throws IOException {
		String path;

		// Standard output -> project folder
		if (outputDir == null || outputDir.isEmpty()) {
			path = fileName + fileEnding;
		} else {
			path = outputDir
					+ (outputDir.endsWith(File.separator) ? "" : File.separator)
					+ fileName + fileEnding;

		}

		File f = new File(path);
		boolean exists = !f.createNewFile();
		log.debug("File " + f.getPath() + " allready existed?: " + exists
				+ " can write?: " + f.canWrite());
		if (!f.canWrite()) {
			throw new IOException("No write permission for File: "
					+ f.getPath());
		}

		log.info(" Created file: " + f.getPath());
		return f;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public synchronized long getWritenBytes() {
		long size = 0;
		// flush();
		for (File f : files) {
			log.debug(" size of file: " + f.getName() + " is  " + f.length());
			size += f.length();
		}
		return size;
	}

	@Override
	protected void configParsers() {
		getNodeParser(NODE_PARSER_max).setRequired(false);
		getNodeParser(NODE_PARSER_min).setRequired(false);
		getNodeParser(NODE_PARSER_rng).setRequired(false);
		getNodeParser(NODE_PARSER_seed).setRequired(false);
		getNodeParser(NODE_PARSER_size).setRequired(false);
		addNodeParser(new OutputDirParser(true, true, this));
		addNodeParser(new DelimiterNodeParser(false, true, this));
		addNodeParser(new BufferSizeNodeParser(false, true, this));
		addNodeParser(new FileEndingNodeParser(false, true, this));
		addNodeParser(new CharsetNodeParser(false, true, this));
		addNodeParser(new LineBufferScaleParser(false, true, this));
		addNodeParser(new EncodeDirectNodeParser(false, true, this));

	}

	private class OwnFileWriter extends Writer {
		private static final int DEFAULT_BUFFER_SIZE = 4098;
		private static final String DEFAULT_CHARSET = "UTF-8";
		private Writer encodingWriter;
		private Writer lineCache;

		/**
		 * Default buffer size 4098 is used;
		 * 
		 * @param file
		 * @throws FileNotFoundException
		 */

		public OwnFileWriter(File file) throws FileNotFoundException {
			this(DEFAULT_BUFFER_SIZE, file, DEFAULT_CHARSET);
		}

		public OwnFileWriter(String file) throws FileNotFoundException {
			this(DEFAULT_BUFFER_SIZE, new File(file), DEFAULT_CHARSET);
		}

		public OwnFileWriter(int bufsize, String file)
				throws FileNotFoundException {
			this(bufsize, new File(file), DEFAULT_CHARSET);

		}

		public OwnFileWriter(File newFile, boolean compress,
				int compressionLevel) throws FileNotFoundException {
			this(newFile);
		}

		public OwnFileWriter(int bufsize, File file, String charset)
				throws FileNotFoundException {

			FileOutputStream fos = new FileOutputStream(file);
			FileChannel fc = fos.getChannel();

			// Writer w is a StreamEncoder;
			encodingWriter = Channels.newWriter(fc, Charset.forName(charset)
					.newEncoder(), bufsize);

			lineCache = new BufferedWriter(encodingWriter, bufsize
					* lineBufScale);

		}

		public void write(char[] cbuf, int off, int len) throws IOException {
			lineCache.write(cbuf, off, len);
		}

		public void writeUnbufferd(char[] cbuf, int off, int len)
				throws IOException {
			encodingWriter.write(cbuf, off, len);
		}

		public void close() throws IOException {
			lineCache.close();
			lineCache = null;
			encodingWriter = null;
		}

		@Override
		public void flush() throws IOException {
			lineCache.flush();
		}
	}

	private class OutputDirParser extends Parser<Element> {

		public OutputDirParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(isRequired, b, NODE_PARSER_outputDir, csvRowOutput,
					"Directory to put the generated files in.");

		}

		@Override
		protected void parse(Node node) throws XmlException {

			outputDir = node.getTextContent();
			File f = null;
			try {
				log.info("Testing specified output dir... ");
				f = newFile("OUTPUT_TEST");
				log.info("Output dir ok!");
				log.info("Writing all files to folder: "
						+ f.getAbsolutePath().substring(0,
								f.getAbsolutePath().indexOf("OUTPUT_TEST")));
				f.delete();
			} catch (IOException e) {
				throw new XmlException(getNodeInfo()
						+ "Could not write to configured output dir: "
						+ outputDir
						+ " Maybe no write permission. Additional info: "
						+ e.getMessage());
			}

		}
	}

	private class EncodeDirectNodeParser extends Parser<Element> {

		public EncodeDirectNodeParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(
					isRequired,
					b,
					NODE_PARSER_encodeDirect,
					csvRowOutput,
					"Enable direct encoding of output files. byte out = (byte) aChar; Values: {true | false} Default: "
							+ encodeDirect);
		}

		@Override
		protected void parse(Node node) throws XmlException {
			encodeDirect = Boolean.parseBoolean(node.getTextContent());
			log.debug("CSVRowOutput - Compression enabled: " + encodeDirect);
		}
	}

	private class DelimiterNodeParser extends Parser<Element> {

		public DelimiterNodeParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(isRequired, b, NODE_PARSER_delimiter, csvRowOutput,
					"Delimiter char used in CSV. F.e.: { , ; | \\t}. Default: "
							+ delimiter);
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String del = node.getTextContent();
			if (del != null && !del.isEmpty() && del.length() == 1) {
				delimiter = del.charAt(0);

			} else {
				throw new XmlException(
						getNodeInfo()
								+ "<"
								+ this.getName()
								+ "> must not be empty and must be exactly one character. "
								+ " value was: " + del);

			}
		}

	}

	private class LineBufferScaleParser extends Parser<Element> {

		public LineBufferScaleParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(isRequired, b, NODE_PARSER_lineBufferScale, csvRowOutput,
					"Scalefactor for BufferSize. Default: " + lineBufScale);

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();

			if (text != null && !text.isEmpty()) {
				try {
					int val = Integer.parseInt(text);

					if (val < 1) {
						throw new NumberFormatException();
					}
					lineBufScale = val;
				} catch (NumberFormatException e) {
					throw new XmlException(
							getNodeInfo()
									+ "<"
									+ this.getName()
									+ "> must not be emtpty and must be  between 1 and "
									+ Integer.MAX_VALUE + " value was: " + text);
				}
			}
		}
	}

	private class BufferSizeNodeParser extends Parser<Element> {

		public BufferSizeNodeParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(
					isRequired,
					b,
					NODE_PARSER_bufferSize,
					csvRowOutput,
					"Buffersize for Output. Buffersize*lineBufferScale is the amount of data cached before a write to the file occurs. Default: "
							+ bufsize);

		}

		@Override
		protected void parse(Node node) throws XmlException {
			String text = node.getTextContent();

			if (text != null && !text.isEmpty()) {
				try {
					int val = Integer.parseInt(text);

					if (val < 1) {
						throw new NumberFormatException();
					}
					bufsize = val;
				} catch (NumberFormatException e) {
					throw new XmlException(
							getNodeInfo()
									+ "<"
									+ this.getName()
									+ "> must not be emtpty and must be  between 1 and "
									+ Integer.MAX_VALUE + " value was: " + text);
				}
			}
		}
	}

	private class FileEndingNodeParser extends Parser<Element> {

		public FileEndingNodeParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(isRequired, b, NODE_PARSER_fileEnding,
					"Ending of output files. Default:" + fileEnding);
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String ending = node.getTextContent();
			if (ending != null && !ending.isEmpty()) {
				// add FILE_ENDING_SEPERATOR if missing in config file
				fileEnding = (ending.startsWith(FILE_ENDING_SEPERATOR + "") ? ""
						: FILE_ENDING_SEPERATOR)
						+ ending;
			}
			log.debug("CSVRowOutput - use File ending for output: "
					+ fileEnding);
		}
	}

	private class CharsetNodeParser extends Parser<Element> {

		public CharsetNodeParser(boolean isRequired, boolean b,
				Element csvRowOutput) {
			super(isRequired, b, NODE_PARSER_charset, csvRowOutput,
					"Charset used for output encoding. Default: " + charset);
		}

		@Override
		protected void parse(Node node) throws XmlException {
			String chars = node.getTextContent();
			if (chars != null && !chars.isEmpty()) {
				// FIXME! check for charset existance;
				if (Charset.isSupported(chars)) {
					charset = Charset.forName(chars);
					log.debug("Using charset \"" + charset
							+ "\" for output files");
				} else {
					throw new XmlException(getNodeInfo() + "<" + this.getName()
							+ "> charset " + chars + " not supported!");
				}
			}
		}
	}

}
