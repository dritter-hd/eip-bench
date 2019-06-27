package com.eipbench.camel;

import com.github.dritter.hd.dlog.evaluator.IFacts;

import java.util.Collection;

public class DatalogProgramCreator {

	private DatalogFacts facts = new DatalogFacts();
	private DatalogMetaFacts metaFacts = new DatalogMetaFacts();


	public DatalogProgramCreator() {
	}
	
	public DatalogProgramCreator(DatalogFacts facts, DatalogMetaFacts metaFacts) {
		this.facts = facts;
		this.metaFacts = metaFacts;
	}


	public DatalogProgram generateDatalogProgram() throws UnsupportedDataTypeException {
		String factString = facts.generateDatalogFactsUsing(metaFacts);
		String metaFactString = metaFacts.generateDatalogFacts();

		Collection<IFacts> datalogFacts = ConversionHelper.parse(factString);
		Collection<IFacts> datalogMetaFacts = ConversionHelper
				.parse(metaFactString);

		return DatalogProgram.create(datalogFacts, datalogMetaFacts);
	}

	public DatalogFacts getFacts() {
		return facts;
	}

	public void setFacts(DatalogFacts facts) {
		this.facts = facts;
	}

	public DatalogMetaFacts getMetaFacts() {
		return metaFacts;
	}

	public void setMetaFacts(DatalogMetaFacts metaFacts) {
		this.metaFacts = metaFacts;
	}
}
