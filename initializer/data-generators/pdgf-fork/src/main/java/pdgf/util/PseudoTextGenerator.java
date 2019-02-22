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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import pdgf.plugin.AbstractPDGFRandom;
import pdgf.util.File.FileHandler;
import pdgf.util.File.LineAccessFile;
import pdgf.util.random.PdgfDefaultRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class PseudoTextGenerator {
	private static PseudoTextGenerator ptg = null;
	private static Properties props;

	private static final String PROPERTIES_FILE = "config/PseudoTextGenerator.properties";
	private static LineAccessFile adjectives;
	private static LineAccessFile adverbs;
	private static LineAccessFile auxiliaries;
	private static LineAccessFile nouns;
	private static LineAccessFile prepositions;
	private static LineAccessFile terminators;
	private static LineAccessFile verbs;

	private static final char[] THE = "the ".toCharArray(); //$NON-NLS-1$

	// private singelton constructor
	private PseudoTextGenerator() {
	}

	/**
	 * Instance of this PseudoTextGenerator.<br/>
	 * Classes using this class are encouraged to safe a reference to this
	 * PseudoTextGenerator to avoid synchronized overhead by calling
	 * PseudoTextGenerator.instance() over and over again. <br/>
	 * <br/>
	 * I know this is a suboptimal solution but the only way to get proper
	 * IOExeption handing while first instantiation. PseudoTextGenerator
	 * localInstanceReference = PseudoTextGenerator.instance();
	 * 
	 * @return the singelton reference to this class
	 * @throws IOException
	 *             can occour when first loading required Files like
	 *             /config/PseudoTextGenerator.properties and the Verb, noun ...
	 *             etc files described in PseudoTextGenerator.properties
	 */
	public static synchronized PseudoTextGenerator instance()
			throws IOException {
		/**
		 * i know this is a suboptimal implementation of the singelton pattern
		 * but the only way i know to do proper Exeption handling while
		 * initializing
		 */
		if (ptg == null) {
			ptg = new PseudoTextGenerator();
			initialize();
		}

		return ptg;
	}

	public static void main(String[] args) throws IOException {
		PseudoTextGenerator pt = instance();

		StringBuilder str = new StringBuilder();
		pt.newSentence(str, new PdgfDefaultRandom());
		System.out.println(str.toString());

	}

	private static void initialize() throws IOException {

		props = new Properties();
		InputStream is = PseudoTextGenerator.class.getClassLoader()
				.getResourceAsStream(PROPERTIES_FILE);

		// config file folder not in classpath!
		if (is == null) {
			System.out.println("ECLIPSE OVERRIDE try direct access for file: "
					+ PROPERTIES_FILE);
			is = new FileInputStream(PROPERTIES_FILE);

		}
		props.load(is);

		adjectives = FileHandler.instance().getLineAccessFile(
				new File(props.getProperty("PseudoTextGenerator.adjectives"))); //$NON-NLS-1$
		adverbs = FileHandler.instance().getLineAccessFile(
				new File(props.getProperty("PseudoTextGenerator.adverbs"))); //$NON-NLS-1$
		auxiliaries = FileHandler.instance().getLineAccessFile(
				new File(props.getProperty("PseudoTextGenerator.auxiliaries"))); //$NON-NLS-1$
		nouns = FileHandler.instance().getLineAccessFile(
				new File(props.getProperty("PseudoTextGenerator.nouns"))); //$NON-NLS-1$
		prepositions = FileHandler
				.instance()
				.getLineAccessFile(
						new File(
								props.getProperty("PseudoTextGenerator.prepositions"))); //$NON-NLS-1$
		terminators = FileHandler.instance().getLineAccessFile(
				new File(props.getProperty("PseudoTextGenerator.terminators"))); //$NON-NLS-1$
		verbs = FileHandler.instance().getLineAccessFile(
				new File(props.getProperty("PseudoTextGenerator.verbs"))); //$NON-NLS-1$
	}

	/**
	 * /** used grammar <br/>
	 * <br/>
	 * 
	 * text:&lt;sentence> |&lt;text> &lt;sentence> ; <br/>
	 * <br/>
	 * 
	 * sentence:&lt;noun phrase> &lt;verb phrase> &lt;terminator> |&lt;noun
	 * phrase> &lt;verb phrase> &lt;prepositional phrase> &lt;terminator>
	 * |&lt;noun phrase> &lt;verb phrase> &lt;noun phrase> &lt;terminator>
	 * |&lt;noun phrase> &lt;prepositional phrase> &lt;verb phrase> &lt;noun
	 * phrase> &lt;terminator> |&lt;noun phrase> &lt;prepositional phrase>
	 * &lt;verb phrase> &lt;prepositional phrase> &lt;terminator> ; <br/>
	 * <br/>
	 * noun phrase:&lt;noun> |&lt;adjective> &lt;noun> |&lt;adjective>,
	 * &lt;adjective> &lt;noun> |&lt;adverb> &lt;adjective> &lt;noun> ; <br/>
	 * <br/>
	 * verb phrase:&lt;verb> |&lt;auxiliary> &lt;verb> |&lt;verb> &lt;adverb>
	 * |&lt;auxiliary> &lt;verb> &lt;adverb>
	 * 
	 * @param str
	 *            Where to write the Sentence to.
	 */
	public void newSentence(StringBuilder str, AbstractPDGFRandom r) {
		int choose = r.nextInt(5);
		switch (choose) {

		case 0:
			nounPhrase(str, r);
			verbPhrase(str, r);

			// <noun phrase> <verb phrase> <terminator>
			break;
		case 1:
			nounPhrase(str, r);
			verbPhrase(str, r);
			prepositionalPhrase(str, r);
			// <noun phrase> <verb phrase> <prepositional phrase> <terminator>
			break;
		case 2:
			nounPhrase(str, r);
			verbPhrase(str, r);
			nounPhrase(str, r);
			// <noun phrase> <verb phrase> <noun phrase> <terminator>
			break;
		case 3:
			nounPhrase(str, r);
			prepositionalPhrase(str, r);
			verbPhrase(str, r);
			nounPhrase(str, r);
			// <noun phrase> <prepositional phrase> <verb phrase> <noun phrase>
			// <terminator>
			break;

		case 4:
			nounPhrase(str, r);
			prepositionalPhrase(str, r);
			verbPhrase(str, r);
			prepositionalPhrase(str, r);
			// <noun phrase> <prepositional phrase> <verb phrase> <prepositional
			// phrase> <terminator> ;
			break;
		}
		getTerminator(str, r);

	}

	private void getWord(StringBuilder str, LineAccessFile file,
			AbstractPDGFRandom r) {
		long lineNo = r.nextLong();
		if (lineNo < 0) {
			lineNo = -lineNo;
		}
		lineNo = lineNo % file.getLineCount();
		str.append(file.getLine(lineNo));
		str.append(' ');
	}

	void verbPhrase(StringBuilder str, AbstractPDGFRandom r) {
		int choose = r.nextInt(4);
		switch (choose) {

		case 0:
			getVerb(str, r);
			break;
		case 1:
			getAuxiliarie(str, r);
			getVerb(str, r);
			// * |<auxiliary> <verb>
			break;
		case 2:
			getVerb(str, r);
			getAdverb(str, r);
			// * |<verb> <adverb>
			break;
		case 3:
			getAuxiliarie(str, r);
			getVerb(str, r);
			getAdverb(str, r);
			// <auxiliary> <verb> <adverb>
			break;

		}

	}

	void nounPhrase(StringBuilder str, AbstractPDGFRandom r) {
		/*
		 * noun phrase: <noun> |<adjective> <noun> |<adjective>, <adjective>
		 * <noun> |<adverb> <adjective> <noun> ;
		 */
		int choose = r.nextInt(4);
		switch (choose) {

		case 0:
			getNoun(str, r);
			break;
		case 1:
			getAdjective(str, r);
			getNoun(str, r);
			break;
		case 2:
			getAdjective(str, r);
			getAdjective(str, r);
			getNoun(str, r);
			break;
		case 3:
			getAdverb(str, r);
			getAdjective(str, r);
			getNoun(str, r);
			break;
		}
	}

	void prepositionalPhrase(StringBuilder str, AbstractPDGFRandom r) {
		// <preposition> the <noun phrase> ;
		getPreposition(str, r);
		str.append(THE);
		nounPhrase(str, r);
	}

	/**
	 * @return the adjectives
	 */
	private void getAdjective(StringBuilder str, AbstractPDGFRandom r) {
		getWord(str, adjectives, r);
	}

	/**
	 * @return the adverbs
	 */
	private void getAdverb(StringBuilder str, AbstractPDGFRandom r) {
		getWord(str, adverbs, r);
	}

	/**
	 * @return the auxiliaries
	 */
	private void getAuxiliarie(StringBuilder str, AbstractPDGFRandom r) {
		getWord(str, auxiliaries, r);
	}

	/**
	 * @return the nouns
	 */
	private void getNoun(StringBuilder str, AbstractPDGFRandom r) {
		getWord(str, nouns, r);
	}

	/**
	 * @return the prepositions
	 */
	private void getPreposition(StringBuilder str, AbstractPDGFRandom r) {
		getWord(str, prepositions, r);
	}

	/**
	 * @return the terminators
	 */
	private void getTerminator(StringBuilder str, AbstractPDGFRandom r) {
		str.deleteCharAt(str.length() - 1);
		getWord(str, terminators, r);
	}

	/**
	 * @return the verbs
	 */
	private void getVerb(StringBuilder str, AbstractPDGFRandom r) {
		getWord(str, verbs, r);
	}

}
