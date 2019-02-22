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

import pdgf.core.dataGenerator.GenerationContext;
import pdgf.generator.DictList;
import pdgf.plugin.AbstractPDGFRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 * 
 */
public class SamplingWithoutReplacement {
	private int[] urn = null;
	private int currentMax = 0;

	public SamplingWithoutReplacement(int startSize) {
		reset(startSize);
	}

	public SamplingWithoutReplacement() {

	}

	/**
	 * Reset or initialize the ballotbox
	 * 
	 * @param startSize
	 */
	public void reset(int startSize) {
		if (urn == null || urn.length != startSize) {
			urn = new int[startSize];
			for (int i = 0; i < urn.length; i++) {
				urn[i] = i;
			}
		}
		currentMax = urn.length - 1;

	}

	/**
	 * Takes the next sample out of the urn.
	 * 
	 * @param parent
	 *            Owning object of this urn, required for getRandomNo in context
	 *            of a distribution plugin
	 * @param r
	 *            the random number generator to be used
	 * @param gc
	 * @return
	 */
	public int takeNextSample(DictList parent, AbstractPDGFRandom r,
			GenerationContext gc) {
		// take a random sample an swap it tu current last place in ballot box
		swap(urn, (int) parent.getRandomNo(r, gc, currentMax + 1), currentMax);
		// return sample wich is a last place, reduce boxSize by 1
		return urn[currentMax--];
	}

	private static void swap(int[] array, int i, int j) {
		int tmp;
		tmp = array[i];
		array[i] = array[j];
		array[j] = tmp;
	}

	public int getStartSize() {
		return urn.length;
	}

	public int getRemainingSize() {
		return currentMax;
	}

}