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

import java.util.Random;

import pdgf.util.random.SkipRandom;

/**
 * Abstract class that extends JDKRandomGenerator extends java.util.Random and
 * implements RandomGenerator and SkipRNG
 * 
 * This abstract class is necessary because PDGF requires a Random number
 * generator that is at minimum compatible to {@linkplain java.util.Random} or
 * AND it must support skip ahead. Skip ahead is absolutely necessary for this
 * Programm. Skip ahead makes it possible to use PDGF in a distributed parallel
 * environment. The workload can be split onto many separate machines. This can
 * only efficiently achieved if we can compute the the n th value of a row of
 * random numbers without computing all n-1 numbers between 1 and n.
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public abstract class AbstractPDGFRandom extends Random implements SkipRandom {

	private static final float LONG_TO_FLOAT_2_POW_64_INV;
	private static final double LONG_TO_DOUBLE_2_POW_64_INV;
	static {
		double y = Math.pow(2, 64);
		LONG_TO_DOUBLE_2_POW_64_INV = 1.0 / y;
		LONG_TO_FLOAT_2_POW_64_INV = (float) LONG_TO_DOUBLE_2_POW_64_INV;
	}

	private static final int BITS_PER_BYTE = 8;
	private static final int BYTES_PER_INT = 4;
	private static final int BYTES_PER_LONG = 4;

	// ################################################
	// <impl of interface methods from SkipRandom>
	// ################################################

	public boolean nextBoolean(long skip) {
		return next(skip) >= 0L;
	}

	public int nextInt(int n, long skip) {
		return (int) (Math.floor(nextDouble() * n));
	}

	public int nextIntSkip(long skip) {
		return (int) (Math.floor(nextDouble(skip) * Integer.MAX_VALUE));
	}

	public float nextFloat(long skip) {
		return (float) next(skip) * LONG_TO_FLOAT_2_POW_64_INV + 0.5f;
	}

	public double nextDouble(long skip) {
		return (double) next(skip) * LONG_TO_DOUBLE_2_POW_64_INV + 0.5;
	}

	public long nextLong(long skip) {
		return next(skip);
	}

	// </ impl of interface methods from SkipRandom>

	// ################################################
	// additional methods of this class
	// ################################################

	/**
	 * reSeeds this prng instace and skips ahead to provided position in PRNG
	 * sequence
	 * 
	 * @param seed
	 * @param skip
	 */
	public void reSeed(long seed, long skip) {
		this.setSeed(seed);
		this.skip(skip);
	}

	/**
	 * resets this RNG to the default seed, skips the provided amount of random
	 * numbers in the random number sequenze and outputs the next following long
	 * value.
	 * 
	 * @param skip
	 * @return
	 */
	public long resetAndNextLong(long skip) {
		setSeed(getSeed());
		return nextLong(skip);
	}

	/**
	 * same as: setSeed(seed); nextLong(skip);
	 * 
	 * @param skip
	 * @param seed
	 * @return
	 */
	public long seedAndNextLong(long skip, long seed) {
		setSeed(seed);
		return nextLong(skip);
	}

	public void nextInts(int[] ints, int range) {
		int bits = (Integer.SIZE - Integer.numberOfLeadingZeros(range));
		range += 1;
		for (int i = 0, len = ints.length; i < len;) {

			for (long rnd = nextLong() >>> 1, n = Math.min(len - i, Long.SIZE
					/ bits); n-- > 0; rnd >>>= bits) {

				ints[i++] = (int) (rnd % range);
			}
		}
	}

	/**
	 * random number in interval [0, max] with max inclusive
	 * 
	 * @param max
	 * @return
	 */
	public long nextLongLimited(long max) {
		long bits, val;
		do {
			bits = next();
			val = bits % max;
		} while (bits - val + (max - 1) < 0L);
		return val;
	}

	// ################################################
	// overrides of methods from java.util.Random
	// ################################################

	@Override
	public long nextLong() {
		return next();
	}

	@Override
	public double nextDouble() {
		return (double) next() * LONG_TO_DOUBLE_2_POW_64_INV + 0.5;

	}

	@Override
	public float nextFloat() {
		return (float) next() * LONG_TO_FLOAT_2_POW_64_INV + 0.5f;
	}

	@Override
	public int nextInt(int n) {
		// assert n > 0 : " n must be > 0";
		return (int) Math
				.floor(((double) next() * LONG_TO_DOUBLE_2_POW_64_INV + 0.5)
						* n);

	}

	@Override
	public int nextInt() {
		return (int) (Math
				.floor(((double) next() * LONG_TO_DOUBLE_2_POW_64_INV)
						* Integer.MAX_VALUE));
	}

	@Override
	protected int next(int bits) {
		return (((int) this.next()) >>> (48 - bits));
	}

	@Override
	public boolean nextBoolean() {
		return next() >= 0L;
	}

	/**
	 * Modified version of {@link java.util.Random#nextBytes(byte[])} operates
	 * on longs instead of ints.</br> can get 4 bytes out of one generated
	 * random number.
	 */
	@Override
	public void nextBytes(byte[] bytes) {
		int numRequested = bytes.length;

		int numGot = 0;
		long rnd = 0;

		while (true) {
			rnd = nextLong();
			for (int i = 0; i < BYTES_PER_LONG; i++) {
				if (numGot == numRequested)
					return;

				rnd >>= BITS_PER_BYTE;
				bytes[numGot++] = (byte) rnd;
			}
		}
	}

	// ################################################
	// To be implemented by subclasses
	// ################################################

	/**
	 * get the inital seed of this PRNG's instance
	 * 
	 * @return
	 */
	public abstract long getSeed();

	/**
	 * (re-)set the inital seed and interal state of this PRNG's instance.
	 * 
	 * @return
	 */
	public abstract void setSeed(long seed);

	/**
	 * This method must not influence the internal state of the RNG (behavior
	 * like a static method!). Behavior is similar to seedAndNextLong(long skip,
	 * long seed) but setSeed(seed) and nextLong(skip) long are not invoked but
	 * simulated without altering the internal state.
	 * 
	 * @param skip
	 * @param seed
	 * @return
	 */
	public abstract long nextLongForSeed(long skip, long seed);

	/**
	 * get the next random sample of the sequence of random numbers <br/>
	 * next(1) equals next();
	 * 
	 * @return
	 */
	protected abstract long next();

	/**
	 * skip ahead in the sequence of random numbers and retrive the next sample<br/>
	 * next(1) equals next();
	 * 
	 * @return
	 */
	protected abstract long next(long skip);
}
