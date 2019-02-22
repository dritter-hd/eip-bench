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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import pdgf.core.Element;
import pdgf.core.FieldValueDTO;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.dbSchema.Field;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidElementException;
import pdgf.core.exceptions.XmlException;
import pdgf.distribution.DistributionFactory;
import pdgf.util.Constants;
import pdgf.util.StaticHelper;
import pdgf.util.File.FileHandler;
import pdgf.util.File.LineAccessFile;

/**
 * Dear Generator Plugin Writer:<br/>
 * Most of the time you do not need to worry about concurrency issues because an
 * instance of your generator is created per thread/worker. The instances of
 * your generator are saved as virtual child's of parent field where Child ID ==
 * thread/worker ID<br/>
 * <br/>
 * Some warnings!<br/>
 * Be very careful using the "static" modifier. Most JDK utility classes (like
 * SimpleDate or other formatter classes) are NOT threadsafe!. Its wise to use
 * "static" only for primitives or own classes where you know exactly how they
 * work.<br/>
 * If you really need a static "state" between your instances (f.e. caching of
 * generated values) you may consider to link it with a specific thread/worker!
 * Perhaps one state or one buffer per thread! The id of the calling thread can
 * be found in "generationContext"{@linkplain GenerationContext#getWorkerID()}
 * "generationContext". You may consider the use of ThreadLocal for this
 * purpose:<br/>
 * <br/>
 * // a ReferenceState per thread <br/>
 * static ThreadLocal&lt;ReferenceState> perThreadReferenceState = new
 * ThreadLocal&lt;ReferenceState>() { <br/>
 * protected ReferenceState initialValue() { <br/>
 * return new ReferenceState(); <br/>
 * } }; <br/>
 * <br/>
 * 
 * The abstract Class Generator is the basic class for all specific data
 * generators. It offers methods for parsing often needed parameters. Further
 * parameters can be included via the xml-subtree. Every implementation of
 * Generator is responsible for parsing is own aditional needed parameters.
 * 
 * @author Michael Frank
 * @version 1.0 13.10.2009
 */
public abstract class Generator extends Element<Distribution, Field> {
	private static final Logger log = LoggerFactory.getLogger(Generator.class);
	// available parsers
	public static final String NODE_PARSER_file = "file";
	public static final String NODE_PARSER_distribution = "distribution";
	private static final int minFileNodes = 1;
	private int currentFileNodes;

	private Distribution distribution = null;
	private ArrayList<File> sourceFile = new ArrayList<File>(1);;
	private ArrayList<LineAccessFile> lineFiles = new ArrayList<LineAccessFile>(
			1);
	private LastValueCache cache = null;
	private FieldValueDTO lastValueCacheTempFieldValueDTO = new FieldValueDTO(
			null, 0);

	private boolean cycleDetected = false;

	// *******************************************************************
	// constructors
	// *******************************************************************

	/**
	 * Creates a emtpy generator; Dont forget to initialize this generator by
	 * adding your parsers to configParsers() and then call
	 * {@link Generator#parseConfig(Node)}
	 * @throws XmlException 
	 */
	public Generator(String description) throws XmlException {

		super("generator", description);

	}

	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);
		cache = new LastValueCache(this.getParent());

	}

	/**
	 * Deer Generator Plugin Writer:<br/>
	 * Most of the time you do not need to worry about concurrency issues
	 * because an instance of your generator is created per thread/worker. The
	 * instances of your generator are saved as virtual child's of parent field
	 * where Child ID == thread/worker ID<br/>
	 * <br/>
	 * Some warnings!<br/>
	 * Be very careful when using the "static" modifier. Most JDK utility
	 * classes (like SimpleDate or other formatter classes) are NOT threadsafe!.
	 * Its wise to use "static" only for primitives or own classes where you
	 * know exactly how they work.<br/>
	 * If you really need a static "state" between your instances (f.e. caching
	 * of generated values) you may consider to link it with a specific
	 * thread/worker! Perhaps one state or one buffer per thread! The id of the
	 * calling thread can be found in "generationContext"
	 * {@linkplain GenerationContext#getWorkerID()} "generationContext". You may
	 * consider the use of ThreadLocal for this purpose:<br/>
	 * <br/>
	 * // a ReferenceState per thread <br/>
	 * static ThreadLocal&lt;ReferenceState> perThreadReferenceState = new
	 * ThreadLocal&lt;ReferenceState>() { <br/>
	 * protected ReferenceState initialValue() { <br/>
	 * return new ReferenceState(); <br/>
	 * } }; <br/>
	 * <br/>
	 * 
	 * The abstract Class Generator is the basic class for all specific data
	 * generators. It offers methods for parsing often needed parameters.
	 * Further parameters can be included via the xml-subtree. Every
	 * implementation of Generator is responsible for parsing is own aditional
	 * needed parameters.
	 * 
	 * @param rng
	 *            the Pseudo Random number generator seeded with a deterministic
	 *            seed unique to each field. to be used if random numbers are
	 *            needed to generate the value. If a distribution is used, this
	 *            rng should be passed down to distribution.nextValue(). (this
	 *            is a reference to {@linkplain this#getElementRng(int
	 *            workerID))} where workerID=
	 *            {@linkplain GenerationContext#getWorkerID()} <br/>
	 *            If <generator> <rng name="OwnRNGClass"> is set, rng will be an
	 *            correctly seeded instance of "OwnRNGClass". If its not set,
	 *            the project default RNG will be used for this purpose.
	 * @param generationContext
	 *            Containing information about the current state of the invoking
	 *            Worker. (f.e. the current row and id of the worker thread)
	 * @param currentFieldValue
	 *            Shared data object! put your generated data in here with:
	 *            currentFieldValue.setValue( Generated result ) <br/>
	 *            Optionally there exits currentFieldValue.setPlainValue(Object
	 *            plainValue). e.g if this generator generate Date or time
	 *            values and output format for value is a string like
	 *            "1999-12-4" plainValue could be long value representing this
	 *            date as unix time in ms. Plain values could be used to store a
	 *            more calculation friendly version to be used by other
	 *            generators referenceing and useing values generated by this
	 *            generator.
	 */
	protected abstract void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue);

	/**
	 * Generate a new value using the provided rng class and writeing the result
	 * into {@link FieldValueDTO#setValue(Object)} Caches the last generated
	 * value for a Row.
	 * 
	 * @param rng
	 * @param generationContext
	 * @param currentFieldValue
	 * @throws IllegalAccessException
	 */
	public void getNextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		assert this.elementID == generationContext.getWorkerID() - 1;
		//
		// if (this.elementID != generationContext.getWorkerID() - 1) {
		// throw new RuntimeException(
		// new IllegalAccessException(
		// "Illegal access. Element id of generator: "
		// + this.elementID
		// + " was not equal to worker id in generationContext "
		// + (generationContext.getWorkerID() - 1))
		// + ". This is not thread safe. Each worker has its own generator");
		//
		// }

		if (generationContext.getCurrentRow() == cache.getRowID()) {
			currentFieldValue.setValue(cache.value);
			currentFieldValue.setPlainValue(cache.plainValue);
		} else {

			if (nullGenerator(rng, generationContext)) {
				currentFieldValue.setPlainValue(null);
				currentFieldValue.setValue(null);

			} else {
				nextValue(rng, generationContext, currentFieldValue);
			}
			cache.updateCache(currentFieldValue,
					generationContext.getCurrentRow());
		}

	}

	/**
	 * Get cached value of this generator
	 * 
	 * @return
	 */
	protected LastValueCache getCachedValue() {
		return cache;
	}

	/**
	 * get cached value of another generator from the same table. Condition is:
	 * Cached values are only available for the current row for intra tuple
	 * dependencies
	 * 
	 * @param fieldID
	 *            id of Field to get cached value from.
	 * @return null if {@link GenerationContext#getCurrentRow()} !=
	 *         {@link LastValueCache#getRowID()}
	 */
	public LastValueCache getCachedValue(int fieldID, GenerationContext gc) {
		Field f = this.getParent().getParent().getChild(fieldID);
		return getCachedValue(f, gc);

	}

	/**
	 * get cached value of another generator from the same table. Condition is:
	 * Cached values are only available for the current row for intra tuple
	 * dependencies
	 * 
	 * @param f
	 *            Field to get cached value from.
	 * @return null if {@link GenerationContext#getCurrentRow()} !=
	 *         {@link LastValueCache#getRowID()}
	 */
	public LastValueCache getCachedValue(Field f, GenerationContext gc) {

		if (cycleDetected) {
			throw new RuntimeException(
					" Cycle found in intra row dependencies! Startpoint: "
							+ this.getNodeInfo());

		}
		cycleDetected = true;

		LastValueCache foreignCache = f.getGenerator(gc.getWorkerID())
				.getCachedValue();

		// check if value is already cached, if not generate it!
		if (foreignCache.getRowID() != gc.getCurrentRow()) {
			lastValueCacheTempFieldValueDTO.setField(f);
			f.getFieldValueForRow(gc, lastValueCacheTempFieldValueDTO);
			// after this, generated values should be updated in foreignCache
		}

		cycleDetected = false;
		return foreignCache;
	}

	protected FieldValueDTO getReferencedValue(int refID, long row,
			GenerationContext gc) {
		return this.getParent().getReference(refID).getReferencedValue(row, gc);

	}

	protected void getReferencedValue(int refID, long row,
			GenerationContext gc, FieldValueDTO fvDTO) {
		this.getParent().getReference(refID).getReferencedValue(row, gc, fvDTO);

	}

	/**
	 * 
	 * Decides if returned value for nextValue() should be null. Subclass should
	 * override this, if another behavior of null generation is desired.
	 * 
	 * 
	 * @param r
	 *            the random number generator used to decide if value should be
	 *            null
	 * @param gc
	 *            metainformation about generation process
	 * @return true if value should be null, false if generator must generate
	 *         another value than null with
	 *         {@linkplain Generator#nextValue(AbstractPDGFRandom, GenerationContext, FieldValueDTO)}
	 */
	protected boolean nullGenerator(AbstractPDGFRandom r, GenerationContext gc) {
		int nullchance = this.getParent().getNullChance();

		// nerver null
		if (nullchance <= 0) {
			return false;
			// allways null
		} else if (nullchance >= 100 * Constants.NULL_CHANCE_PRECISION) {
			return true;

		} else {
			if (r.nextInt(100 * Constants.NULL_CHANCE_PRECISION) < nullchance) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * File to get Values from in the generation Process. This is a optional
	 * Parameter. so it may be null
	 * 
	 * @return File
	 */
	public File getFilePath() {
		return sourceFile.get(0);
	}

	/**
	 * File to get Values from in the generation Process. This is a optional
	 * Parameter. so it may be null
	 * 
	 * @return File
	 */
	public File getFilePath(int id) {
		return sourceFile.get(id);
	}

	public int getFileCount() {
		return lineFiles.size();
	}

	/**
	 * Get distribution function of this Generator. If a Generator is set this
	 * method equals getChild()
	 * 
	 * @return the distribution
	 */
	public Distribution getDistribution() {
		return distribution;
	}

	/**
	 * get the file as specified in &lt;file>path/filename&lt;/filename> (or
	 * first file if more than one files are specified in config)
	 * 
	 * @return
	 */
	public LineAccessFile getFile() {
		return lineFiles.get(0);
	}

	/**
	 * get the file as specified in &lt;file>path/filename&lt;/filename> (or
	 * first file if more than one files are specified in config)
	 * 
	 * @return
	 */
	public LineAccessFile getFile(int id) {
		return lineFiles.get(id);
	}

	public void addFileNodeParser() {

		// nasty hack. This method gets called before constructor of this
		// Instances has finished. Therefore local fields are not yet
		// initialized.
		if (currentFileNodes == 0) {
			currentFileNodes = minFileNodes;
		}
		addNodeParser(new FileNodeParser(
				(NODE_PARSER_file + (currentFileNodes++)), false, false, this));
	}

	private void loadFile(String filename) throws XmlException {
		File file = new File(filename);

		try {
			lineFiles.add(FileHandler.instance().getLineAccessFile(file));
			sourceFile.add(file);
		} catch (IOException e) {
			throw new XmlException(getNodeInfo() + " error loading file: "
					+ e.getMessage());
		}
	}

	protected void configParsers() throws XmlException{
		addNodeParser(new FileNodeParser(NODE_PARSER_file, false, false, this));
		addNodeParser(new DistributionNodeParser(false, false, this));
	}
	/**
	 * The parent field of this generator.
	 * 
	 * @return
	 */
	public Field getParentField() {
		return getParent();
	}
	private class FileNodeParser extends Parser<Generator> {

		public FileNodeParser(String nodeName, boolean isRequired,
				boolean used, Generator parent) {
			super(
					isRequired,
					used,
					nodeName,
					parent,
					"Default for all Generators. A path to a file to be used by a generator. See pdgf.generator.DictList source for a example.");
		}

		@Override
		protected void parse(Node node) throws XmlException {

			String filename = node.getTextContent();

			if (filename != null && !filename.isEmpty()) {
				loadFile(filename);
			} else if (this.isRequired()) {
				throw new XmlException(getNodeInfo() + "<" + this.getName()
						+ "> is required but empty! ");
			}
		}

	}

	private class DistributionNodeParser extends Parser<Generator> {

		public DistributionNodeParser(boolean isRequired, boolean used,
				Generator parent) {
			super(isRequired, used, NODE_PARSER_distribution, parent,
					"Distribution to be used by a generator when calculating a value");

		}

		@Override
		protected void parse(Node node) throws XmlException {

			// Parse only fist occurrence of <distribution> node
			if (distribution == null) {
				String name = StaticHelper.getNodeNameAttr(node,
						this.getParent());

				if (name == null || name.isEmpty()) {
					if (this.isRequired()) {
						throw new XmlException(getNodeInfo() + "<"
								+ this.getName()
								+ "> is required but name attribute was empty.");
					}
					log.debug(getNodeInfo()
							+ " Distribution node found but Ignored because \"name\" attribute was empty");
				} else {
					distribution = DistributionFactory.instance()
							.getDistribution(node, this.getParent());

					// init childs
					setChilds(1, distribution.getClass());
					try {
						addChild(distribution, 0);
					} catch (InvalidElementException e) {
						throw new XmlException(e.getMessage());
					}
				}
			}
		}
	}

	public class LastValueCache {
		private Object value = null;
		private Object plainValue = null;
		private int type;
		private Field field = null;
		private long row;
		private long lastRandomRow = Constants.LONG_NOT_SET;

		private LastValueCache(Field f) {

			this.field = f;
			this.type = f.getTypeID();
		}

		void updateCache(FieldValueDTO currentFieldValue, long row) {
			value = currentFieldValue.getValue();
			plainValue = currentFieldValue.getPlainValue();
			this.row = row;
		}

		/**
		 * generated value as in {@linkplain FieldValueDTO#getValue()}
		 * 
		 * @return
		 */
		public Object getCachedValue() {
			return value;
		}

		/**
		 * plain representation of generated value as in
		 * {@linkplain FieldValueDTO#getPlainValue()}
		 * 
		 * @return
		 */
		public Object getCachedPlainValue() {
			return plainValue;
		}

		public int getTypeOfValue() {
			return type;
		}

		/**
		 * field the value in this cache was generated for
		 * 
		 * @return
		 */
		public Field getField() {
			return field;
		}

		/**
		 * row this cache entry belongs to
		 * 
		 * @return
		 */
		public long getRowID() {
			return row;
		}

		/**
		 * If this generator requires a value from another generator, he maybe
		 * has to randomly chose a row he wishes to get the value from. This row
		 * ID should be stored here. Other generators of the same table may can
		 * use this value to select the same row again.
		 * 
		 * @param lastRandomRow
		 */
		public void setLastRandomRow(long lastRandomRow) {
			this.lastRandomRow = lastRandomRow;
		}

		/**
		 * If this generator requires a value from another generator, he maybe
		 * has to randomly chose a row he wishes to get the value from. This row
		 * ID should be stored here. Other generators of the same table may can
		 * use this value to select the same row again.
		 * 
		 * @param lastRandomRow
		 * @return Constants.LONG_NOT_SET if this feature is not used. else: the
		 *         cached last random row
		 */
		public long getLastRandomRow() {
			return lastRandomRow;
		}

	}

}
