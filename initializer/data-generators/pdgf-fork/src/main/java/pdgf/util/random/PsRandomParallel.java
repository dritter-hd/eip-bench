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
package pdgf.util.random;

import java.util.Random;

import flanagan.math.PsRandom;

/**
 * Extends {@link flanagan.math.PsRandom} permits the use of alternative Rngs in
 * {@link flanagan.math.PsRandom} by by overriding nextGaussian(),
 * nextGaussian(double mean, double sd), nextDouble() and setSeed(long seed).
 * These methods mentioned above are used by all other distribution functions to
 * calculate their results. By overriding them it is possible to replace the
 * hard coded java.util.Random in PsRandom with a subclass of it. The use of
 * other, maybe statistically better RNG is possible.
 * 
 * @see flanagan.math.PsRandom
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public class PsRandomParallel extends PsRandom {
	private Random rr;
	private long initialSeed;

	public PsRandomParallel(Random rr, long seed) {
		super();
		super.setSeed(seed);
		this.rr = rr;
		this.rr.setSeed(seed);
		this.initialSeed = seed;
	}

	/**
	 * Enables the use of other RNG than java.util.Random in PsRandom <br/>
	 * see: {@linkplain PsRandom#nextGaussian()}
	 */
	@Override
	public double nextGaussian() {
		return this.rr.nextGaussian();
	}

	/**
	 * Enables the use of other RNG than java.util.Random in PsRandom <br/>
	 * see: {@linkplain PsRandom#nextGaussian(double mean, double sd)}
	 */
	@Override
	public double nextGaussian(double mean, double sd) {

		return rr.nextGaussian() * sd + mean;
	}

	/**
	 * Enables the use of other RNG than java.util.Random in PsRandom <br/>
	 * see: {@linkplain PsRandom#nextDouble()}
	 */
	@Override
	public double nextDouble() {

		return this.rr.nextDouble();
	}

	public void setRandom(Random rr) {
		this.rr = rr;
	}

	public void setRandom(Random rr, long seed) {
		this.rr = rr;
		this.rr.setSeed(initialSeed);
		this.initialSeed = seed;
	}

	/**
	 * Reseeds this class internal RNG and sets initialseed to value of new seed
	 * 
	 * @param seed
	 *            the new seed
	 */
	@Override
	public void setSeed(long seed) {
		super.setSeed(seed);
		this.rr.setSeed(seed);
		this.initialSeed = seed;
	}

	@Override
	public long getInitialSeed() {
		return initialSeed;
	}

}
