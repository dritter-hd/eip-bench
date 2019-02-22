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

import flanagan.math.PsRandom;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Distribution;

/**
 * Normal/Gaussian distribution from
 * {@link PsRandom#nextGaussian(double, double)}
 * 
 * @author Michael Frank
 * @version 1.0 16.12.2009
 * 
 */
public class Normal extends Distribution {
	protected void configParsers() {
		super.configParsers();
		this.getNodeParser(NODE_PARSER_mu).setRequired(false).setUsed(true);
		this.getNodeParser(NODE_PARSER_sigma).setRequired(false).setUsed(true);
	}

	public Normal() throws XmlException {
		super(
				"Calculates normal(gaussian) distribued values. mu and sd can be specified but are not needed");
	}

	@Override
	public double nextDoubleValue(AbstractPDGFRandom rng,
			GenerationContext generationContext) {

		if (Double.isNaN(mu) || Double.isNaN(sigma)) {
			return rng.nextGaussian(); // fast
			// return distribution.nextGaussian();//slow
		} else {
			return rng.nextGaussian() * sigma + mu; // fast
			// return distribution.nextGaussian(mu, sigma);
		}

	}

	@Override
	public int nextIntValue(AbstractPDGFRandom r, GenerationContext context) {

		if (Double.isNaN(mu) || Double.isNaN(sigma)) {
			return (int) Math.round(r.nextGaussian());
		} else {
			return (int) Math.round(r.nextGaussian() * sigma + mu);
		}

	}

	@Override
	public long nextLongValue(AbstractPDGFRandom r, GenerationContext context) {

		if (Double.isNaN(mu) || Double.isNaN(sigma)) {
			return (long) Math.round(r.nextGaussian());
		} else {
			return (long) Math.round(r.nextGaussian() * sigma + mu);
		}
	}

	@Override
	public long nextLongPositiveValue(AbstractPDGFRandom r,
			GenerationContext context) {
		if (Double.isNaN(mu) || Double.isNaN(sigma)) {
			return (long) Math.round(r.nextGaussian());
		} else {
			return (long) Math.round(r.nextGaussian() * sigma + mu);
		}
	}

}
