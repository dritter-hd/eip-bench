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
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.plugin.Distribution;
import flanagan.math.PsRandom;

/**
 * LogNormal distribution from {@link PsRandom#nextLogNormal(double, double)}
 * 
 * @author Michael Frank
 * @version 1.0 19.12.2009
 * 
 */
public class LogNormalFast extends Distribution {

	public LogNormalFast() throws XmlException {
		super("Calculates logNormal distributed values.");

	}

	private int scaleFactor;

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		// TODO Auto-generated method stub
		super.initialize(workers);

		scaleFactor = this.getParent().getParent().getParent().getParent()
				.getScaleFactor();
	}

	@Override
	protected void configParsers() {
		super.configParsers();
		this.getNodeParser(NODE_PARSER_mu).setRequired(true).setUsed(true);
		this.getNodeParser(NODE_PARSER_sigma).setRequired(true).setUsed(true);
	}

	@Override
	public double nextDoubleValue(AbstractPDGFRandom rng,
			GenerationContext generationContext) {

		return nextLogNormal(rng);

	}

	@Override
	public int nextIntValue(AbstractPDGFRandom r, GenerationContext context) {

		return (int) Math.round(nextLogNormal(r));
	}

	@Override
	/**
	 * Only positive!
	 */
	public long nextLongValue(AbstractPDGFRandom r, GenerationContext context) {

		return (long) Math.round(nextLogNormal(r));
	}

	@Override
	public long nextLongPositiveValue(AbstractPDGFRandom r,
			GenerationContext context) {
		return (long) Math.round(nextLogNormal(r));
	}

	private double nextLogNormal(AbstractPDGFRandom rng) {

		/*
		 * 
		 * 
		 * Step 1
		 * 
		 * Assuming the required distribution is defined in terms of the mean
		 * and standard deviation of the population, use equations 122.1 and
		 * 122.2 to derive meanlog and stdevlog.
		 * 
		 * Step 2
		 * 
		 * Generate standard normally distributed random number:
		 * 
		 * r1=Sqr(-2 * Log(rnd())) * Sin(2 * PI * rnd())
		 * 
		 * Step 3
		 * 
		 * Scale the standard value with meanlog and stdevlog:
		 * 
		 * r1=meanlog+r1*stdevlog
		 * 
		 * Step 4
		 * 
		 * Exponentiate r1 to create a lognormal value:
		 * 
		 * r1=exp(r1)
		 */
		return Math.exp(mu + Math.sqrt(sigma) * rng.nextDouble());
		// return Math.exp((rng.nextGaussian() * sigma) + mu) * scaleFactor;
	}

}
