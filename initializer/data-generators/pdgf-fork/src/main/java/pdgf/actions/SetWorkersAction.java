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

import pdgf.core.dbSchema.Project;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.InvalidArgumentException;
import pdgf.core.exceptions.NotSupportedException;
import pdgf.core.exceptions.XmlException;
import pdgf.util.Constants;

public class SetWorkersAction extends Action {

	public SetWorkersAction() {
		super(
				"workers",
				"<number>",
				"number of threads/workers to be used (optional). Overrides automatic worker determination by cpu count",
				1, 1);
	}

	@Override
	public void execute(String[] tokens) throws XmlException,
			ConfigurationException, InvalidArgumentException {
		checkParamQuantity(tokens);
		if (!dataGen.isStarted()) {
			if (project == null) {
				project = new Project();
			}
			int number = Constants.INT_NOT_SET;
			number = Integer.parseInt(tokens[1]);
			if (number < 1)
				throw new InvalidArgumentException("ERROR! Workers \""
						+ tokens[1] + "\" must be >=1 ");

			log.info("Set Workers from " + project.getWorkers() + " to: "
					+ number);
			project.setWorkers(number);
		} else {
			throw new NotSupportedException(
					"Cannot load config file while generator is running");
		}
	}
}