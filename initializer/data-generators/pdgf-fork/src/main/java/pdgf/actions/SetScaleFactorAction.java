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
package pdgf.actions;

import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.NotSupportedException;
import pdgf.core.exceptions.XmlException;
import pdgf.util.Constants;

public class SetScaleFactorAction extends Action {

	public SetScaleFactorAction() {
		super(
				"scaleFactor",
				"<sf>",
				" scale factor  for the project. Project config file must be loaded before scale factor can be changed",
				1, 1);
	}

	@Override
	public void execute(String[] tokens) throws XmlException,
			InvalidArgumentException,
			pdgf.core.exceptions.ConfigurationException {
		checkParamQuantity(tokens);
		if (!dataGen.isStarted()) {
			if (project.projectConfigLoaded()) {
				int number = Constants.INT_NOT_SET;
				number = Integer.parseInt(tokens[1]);
				if (number < 1)
					throw new InvalidArgumentException("ERROR! scale factor \""
							+ tokens[1] + "\" must be between [1, "
							+ Integer.MAX_VALUE + "] ");

				log.info("Set scale factor from " + project.getScaleFactor()
						+ " to " + number);
				project.setScaleFactor(number);

			} else {
				throw new pdgf.core.exceptions.ConfigurationException(
						"ERROR! Project config xml must be loaded before changing of scale factor can be done");
			}
		} else {
			throw new NotSupportedException(
					"Cannot load config file while generator is running");
		}
	}
}