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
package pdgf.distribution;

import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Distribution;
import pdgf.util.random.PdgfDefaultRandom;

/**
 * Uniform distribution from {@link PdgfDefaultRandom#nextDouble()} or
 * {@link PdgfDefaultRandom#nextInt()} or {@link PdgfDefaultRandom#nextLong()}
 * 
 * @author Michael Frank
 * @version 1.0 19.12.2009
 * 
 */
public class Uniform extends Distribution {

	public Uniform() throws XmlException {
		super(
				"Calculates uniformly distributed values. Output is equal to the distribution produced by used RandomNumberGenerator r. F.e.: r.nextDouble(); r.nextInt() ");

	}

	protected void configParsers() {
		super.configParsers();
		this.getNodeParser(NODE_PARSER_mu).setRequired(false).setUsed(false);
		this.getNodeParser(NODE_PARSER_sigma).setRequired(false).setUsed(false);
	}

	@Override
	public double nextDoubleValue(AbstractPDGFRandom rng,
			GenerationContext generationContext) {
		if (max == pdgf.util.Constants.LONG_NOT_SET
				|| min == pdgf.util.Constants.LONG_NOT_SET) {
			return rng.nextDouble();
		} else {
			if (max <= 1) {
				return rng.nextDouble();
			} else {
				return min + (rng.nextDouble() * (max - min));
			}
		}

	}

	@Override
	public int nextIntValue(AbstractPDGFRandom r, GenerationContext context) {

		if (max == pdgf.util.Constants.LONG_NOT_SET
				|| min == pdgf.util.Constants.LONG_NOT_SET) {
			return r.nextInt();
		} else if (max > min && max < Integer.MAX_VALUE) {

			return (int) (min + (r.nextInt((int) (max - min))));

		} else {
			// FIXME! maybe error?
			return r.nextInt();
		}

	}

	@Override
	public long nextLongValue(AbstractPDGFRandom r, GenerationContext context) {
		if (max == pdgf.util.Constants.LONG_NOT_SET || (max == 0 && min == 0)
				|| min == pdgf.util.Constants.LONG_NOT_SET) {
			return r.nextLong();
		} else {
			if (min > 0) {// FIXME! check this
				return min + (Math.abs(r.nextLong()) % (max - min));
			} else {
				return min + (r.nextLong() % (max - min));
			}

		}
	}

	@Override
	public long nextLongPositiveValue(AbstractPDGFRandom r,
			GenerationContext context) {
		if (max == pdgf.util.Constants.LONG_NOT_SET || (max == 0 && min == 0)
				|| min == pdgf.util.Constants.LONG_NOT_SET) {
			return Math.abs(r.nextLong());
		} else {
			if (min > 0) {// FIXME! check this
				return min + (Math.abs(r.nextLong()) % (max - min));
			} else {
				return min + (Math.abs(r.nextLong()) % (max - min));
			}

		}
	}
}
