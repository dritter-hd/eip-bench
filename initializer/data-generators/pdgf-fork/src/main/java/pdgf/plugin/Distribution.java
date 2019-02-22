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

import org.w3c.dom.Node;

import pdgf.core.Element;
import pdgf.core.Parser;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.NotSupportedException;
import pdgf.core.exceptions.XmlException;
import pdgf.generator.DefaultReferenceGenerator;
import pdgf.util.StaticHelper;
import pdgf.util.random.PsRandomParallel;

/**
 * 
 * Distribution Plugin Writer:<br/>
 * Warning first!
 * {@link Distribution#nextDoubleValue(AbstractPDGFRandom, GenerationContext)}
 * is called by many different threads! its best that you think of it as
 * "static".<br/>
 * It is generally wise to avoid Synchronization if possible because it is a
 * huge performance bottleneck! If you really! need a "state" (f.e.: a counter,
 * buffer etc.) you may consider it to link it with a specific thread! Perhaps
 * one state per thread, or one buffer per thread! The id of the calling thread
 * can be found in "generationContext". You may also consider the use of
 * ThreadLocal for this purpose.<br/>
 * See implementation of
 * {@link DefaultReferenceGenerator#nextValue(AbstractPDGFRandom, GenerationContext, pdgf.core.FieldValueDTO)}
 * for a example use of ThreadLocal <br/>
 * 
 * 
 * Encapsulates a Distribution Function
 * 
 * @param <Type>
 *            specifies the return Type of this generators nextValue() method
 * 
 * @author Michael Frank
 * @version 1.0 18.11.2009
 */
public abstract class Distribution extends Element<Element, Generator> {
	// Parser for nodes <mu> and <sigma>
	public static final String NODE_PARSER_mu = "mu";
	public static final String NODE_PARSER_sigma = "sigma";

	protected double mu = Double.NaN;
	protected double sigma = Double.NaN;;

	// default distribution
	public PsRandomParallel distribution;

	public Distribution(String description) throws XmlException {
		super("distribution", description);
		distribution = null;
	}

	@Override
	public void parseConfig(Node node) throws XmlException {
		super.parseConfig(node);
		if (this.getSeed() == null) {
			seed = System.currentTimeMillis();
		}
		distribution = new PsRandomParallel(getNewElementRng(this.getParent()
				.getParent().getParent().getParent(), seed), seed);
	}

	/**
	 ** 
	 * Deer Distribution Plugin Writer:<br/>
	 * Warning first! this method is called by many different threads! its best
	 * that you think of it as "static".<br/>
	 * It is generally wise to avoid Synchronization if possible because it is a
	 * huge performance bottleneck! If you really! need a "state" you may
	 * consider it to link it with a specific thread! Perhaps one state per
	 * thread, or one buffer per thread! The id of the calling thread can be
	 * found in "generationContext". You may consider the use of ThreadLocal.<br/>
	 * <br/>
	 * This should be the default way to get the next value<br/>
	 * Get the next value with the given PRNG RNG is received from the Parent
	 * Generator object.
	 * 
	 * @param rng
	 *            PRNG to use, if r==null, use your own or Projects default rng
	 * @return the next Value
	 * @throws NotSupportedException
	 *             if this method is not implemented
	 */
	abstract public double nextDoubleValue(AbstractPDGFRandom rng,
			GenerationContext generationContext);

	/**
	 ** 
	 * Deer Distribution Plugin Writer:<br/>
	 * Warning first! this method is called by many different threads! its best
	 * that you think of it as "static".<br/>
	 * It is generally wise to avoid Synchronization if possible because it is a
	 * huge performance bottleneck! If you really! need a "state" you may
	 * consider it to link it with a specific thread! Perhaps one state per
	 * thread, or one buffer per thread! The id of the calling thread can be
	 * found in "generationContext". You may consider the use of ThreadLocal.<br/>
	 * <br/>
	 * This should be the default way to get the next value<br/>
	 * Get the next value with the given PRNG RNG is received from the Parent
	 * Generator object.
	 * 
	 * @param rng
	 *            PRNG to use, if r==null, use your own or Projects default rng
	 * @return the next Value from Long.minValue() to Long.maxValue()
	 * @throws NotSupportedException
	 *             if this method is not implemented
	 */
	abstract public long nextLongValue(AbstractPDGFRandom rng,
			GenerationContext generationContext);

	/**
	 ** 
	 * Deer Distribution Plugin Writer:<br/>
	 * Warning first! this method is called by many different threads! its best
	 * that you think of it as "static".<br/>
	 * It is generally wise to avoid Synchronization if possible because it is a
	 * huge performance bottleneck! If you really! need a "state" you may
	 * consider it to link it with a specific thread! Perhaps one state per
	 * thread, or one buffer per thread! The id of the calling thread can be
	 * found in "generationContext". You may consider the use of ThreadLocal.<br/>
	 * <br/>
	 * This should be the default way to get the next value<br/>
	 * Get the next value with the given PRNG RNG is received from the Parent
	 * Generator object.
	 * 
	 * @param rng
	 *            PRNG to use, if r==null, use your own or Projects default rng
	 * @return the next positive only long value
	 * @throws NotSupportedException
	 *             if this method is not implemented
	 */
	abstract public long nextLongPositiveValue(AbstractPDGFRandom rng,
			GenerationContext generationContext);

	/**
	 ** 
	 * Deer Distribution Plugin Writer:<br/>
	 * Warning first! this method is called by many different threads! its best
	 * that you think of it as "static".<br/>
	 * It is generally wise to avoid Synchronization if possible because it is a
	 * huge performance bottleneck! If you really! need a "state" you may
	 * consider it to link it with a specific thread! Perhaps one state per
	 * thread, or one buffer per thread! The id of the calling thread can be
	 * found in "generationContext". You may consider the use of ThreadLocal.<br/>
	 * <br/>
	 * This should be the default way to get the next value<br/>
	 * Get the next value with the given PRNG RNG is received from the Parent
	 * Generator object.
	 * 
	 * @param rng
	 *            PRNG to use, if r==null, use your own or Projects default rng
	 * @return the next Value from 0 to Integer.maxValue()
	 * 
	 * @throws NotSupportedException
	 *             if this method is not implemented
	 */
	abstract public int nextIntValue(AbstractPDGFRandom rng,
			GenerationContext generationContext);

	// TODO, THREAD SAFETY?

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public double getSigma() {
		return sigma;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double getMu() {
		return mu;
	}

	/**
	 * Return the integer value from the next pseudorandom value in this PRNG's
	 * sequence. Each value in the range 0 through <I>N</I>-1 is returned with a
	 * probability of 1/<I>N</I>.
	 * 
	 * @param n
	 *            Range of values to return.
	 * 
	 * @return Integer value in the range 0 through <I>N</I>-1 inclusive.
	 */
	public static int doubleToInt(double d, int n) {
		return (int) Math.floor(d * n);
	}

	/**
	 * Return the integer value from the next pseudorandom value in this PRNG's
	 * sequence. Each value in the range 0 through <I>N</I>-1 is returned with a
	 * probability of 1/<I>N</I>.
	 * 
	 * @param n
	 *            Range of values to return.
	 * 
	 * @return Long value in the range 0 through <I>N</I>-1 inclusive.
	 */
	public static long doubleToLong(double d, long n) {
		return (long) Math.floor(d * n);
	}

	@Override
	protected void configParsers() {

		addNodeParser(new MuFieldNodeParser(false, false, this));
		addNodeParser(new SigmaFieldNodeParser(false, false, this));

	}

	private class MuFieldNodeParser extends Parser<Distribution> {

		public MuFieldNodeParser(boolean required, boolean used,
				Distribution parent) {
			super(
					required,
					used,
					NODE_PARSER_mu,
					parent,
					"Default for all Distributions. mu Value of a Distributin. (See Normal distribution for example use)");

		}

		@Override
		protected void parse(Node node) throws XmlException {
			try {
				setMu(StaticHelper.parseDoubleTextContent(getNodeInfo(), node));
			} catch (XmlException e) {
				if (this.isRequired()) {
					throw e;
				}
			}
		}
	}

	private class SigmaFieldNodeParser extends Parser<Distribution> {

		public SigmaFieldNodeParser(boolean required, boolean used,
				Distribution parent) {
			super(
					required,
					used,
					NODE_PARSER_sigma,
					parent,
					"Default for all Distributions. mu Value of a Distributin. (See Normal distribution for example use)");

		}

		@Override
		protected void parse(Node node) throws XmlException {

			try {
				setSigma(StaticHelper.parseDoubleTextContent(getNodeInfo(),
						node));
			} catch (XmlException e) {
				if (this.isRequired()) {
					throw e;
				}
			}
		}
	}
}
