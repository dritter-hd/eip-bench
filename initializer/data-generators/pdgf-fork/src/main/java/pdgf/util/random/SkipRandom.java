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

/**
 * Interface for a Pseudo Random Number Generator supporting Skip Ahead
 * 
 * @author Michael Frank
 * @version 1.0 08.10.2009
 */
public interface SkipRandom {

	/**
	 * Set this PRNG's seed.
	 * <P>
	 * <I>Note:</I> Depending on the PRNG algorithm, certain seed values may not
	 * be allowed. See the PRNG algorithm subclass for further information.
	 * 
	 * @param seed
	 *            Seed.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if the PRNG algorithm does
	 *                not allow the given seed value.
	 */
	public abstract void setSeed(long seed);

	/**
	 * Skip one position ahead in this PRNG's sequence.
	 */
	public void skip();

	public void autoSkip(boolean doAutoSkip);

	public void setAutoSkipStep(long skip);

	/**
	 * Skip the given number of positions ahead in this PRNG's sequence. If
	 * <TT>skip</TT> &lt;= 0, the <TT>skip()</TT> method does nothing.
	 * 
	 * @param skip
	 *            Number of positions to skip.
	 */
	public void skip(long skip);

	/**
	 * Return the Boolean value from the pseudorandom value the given number of
	 * positions ahead in this PRNG's sequence. With a probability of 0.5
	 * <TT>true</TT> is returned, with a probability of 0.5 <TT>false</TT> is
	 * returned.
	 * 
	 * @param skip
	 *            Number of positions to skip.
	 * 
	 * @return Boolean value.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>skip</TT> &lt;= 0.
	 */
	public boolean nextBoolean(long skip);

	/**
	 * Return the integer value from the pseudorandom value the given number of
	 * positions ahead value in this PRNG's sequence. Each value in the range 0
	 * through <I>N</I>-1 is returned with a probability of 1/<I>N</I>.
	 * 
	 * @param n
	 *            Range of values to return.
	 * @param skip
	 *            Number of positions to skip.
	 * 
	 * @return Integer value in the range 0 through <I>N</I>-1 inclusive.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <I>N</I> &lt;= 0. Thrown
	 *                if <TT>skip</TT> &lt;= 0.
	 */
	public int nextInt(int n, long skip);

	public int nextIntSkip(long skip);

	/**
	 * Return the single precision floating point value from the pseudorandom
	 * value the given number of positions ahead in this PRNG's sequence. The
	 * returned numbers have a uniform distribution in the range 0.0 (inclusive)
	 * to 1.0 (exclusive).
	 * 
	 * @param skip
	 *            Number of positions to skip.
	 * 
	 * @return Float value.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>skip</TT> &lt;= 0.
	 */
	public float nextFloat(long skip);

	/**
	 * Return the double precision floating point value from the pseudorandom
	 * value the given number of positions ahead in this PRNG's sequence. The
	 * returned numbers have a uniform distribution in the range 0.0 (inclusive)
	 * to 1.0 (exclusive).
	 * 
	 * @param skip
	 *            Number of positions to skip.
	 * 
	 * @return Double value.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>skip</TT> &lt;= 0.
	 */
	public double nextDouble(long skip);

	/**
	 * Return the 64-bit pseudorandom value the given number of positions ahead
	 * in this PRNG's sequence.
	 * 
	 * @param skip
	 *            Number of positions to skip.
	 * @return Long Value
	 */
	public long nextLong(long skip);

	// /**
	// * Not Supported!
	// * @param bytes
	// */
	// public void nextBytes(byte[] bytes, long skip);

}
