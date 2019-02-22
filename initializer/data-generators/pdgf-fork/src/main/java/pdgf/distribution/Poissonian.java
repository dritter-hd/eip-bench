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
import pdgf.core.exceptions.NotSupportedException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Distribution;
import flanagan.math.PsRandom;

/**
 * Poissonian distribution from {@link PsRandom#nextPoissonian(double)}
 * 
 * @author Michael Frank
 * @version 1.0 19.12.2009
 * 
 */
public class Poissonian extends Distribution {
	public Poissonian() throws XmlException {
		super("Calculates poissonian distributed values.");

	}

	@Override
	protected void configParsers() {
		super.configParsers();
		this.getNodeParser(NODE_PARSER_mu).setRequired(true);
		this.getNodeParser(NODE_PARSER_sigma).setRequired(false).setUsed(false);
	}

	@Override
	public double nextDoubleValue(AbstractPDGFRandom rng,
			GenerationContext generationContext) {
		if (rng != null) {
			distribution.setRandom(rng);
		}

		return distribution.nextPoissonian(mu);

	}

	@Override
	public int nextIntValue(AbstractPDGFRandom r, GenerationContext context) {
		if (r != null) {
			distribution.setRandom(r);
		}

		return (int) Math.round(distribution.nextPoissonian(mu));
	}

	@Override
	public long nextLongValue(AbstractPDGFRandom r, GenerationContext context) {
		if (r != null) {
			distribution.setRandom(r);
		}

		return (long) Math.round(distribution.nextPoissonian(mu));
	}

	@Override
	public long nextLongPositiveValue(AbstractPDGFRandom r,
			GenerationContext context) {
		throw new NotSupportedException(
				"This method is not supported by this distribution type");
	}

}
