package com.eipbench.camel;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class CsvDatalogDataFormat implements DataFormat {
	String filename;
	private String delimiter;
	private String relationName;

	public CsvDatalogDataFormat(String delimiter, String relationName) {
		this.delimiter = delimiter;
		this.relationName = relationName;
	}

	public void marshal(Exchange exchange, Object graph, OutputStream stream)
			throws Exception {
		throw new NotYetImplementedException();
	}

	public Object unmarshal(Exchange exchange, InputStream stream)
			throws Exception {

		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader bufferedReader = new BufferedReader(reader);

		// generate MetaFacts from first line of file
		String parameterNames = bufferedReader.readLine();
		String[] parameters = parameterNames.split(delimiter);
		DatalogMetaFacts metaFacts = new DatalogMetaFacts();
		DatalogMetaFact metaFact = new DatalogMetaFact();
		for (String parameter : parameters) {
			metaFact.addParameter(parameter);
		}
		metaFacts.add(relationName, metaFact);

		DatalogFacts facts = new DatalogFacts();
		while (bufferedReader.ready()) {
			String factValues = bufferedReader.readLine();
			String[] parameterValues = factValues.split(delimiter);

			DatalogFact fact = new DatalogFact();
			for (String parameterValue : parameterValues) {
				fact.insertParamater(parameterValue);
			}
			facts.add(relationName, fact);
		}
		
		bufferedReader.close();
		reader.close();

		DatalogProgramCreator creator = new DatalogProgramCreator(facts,
				metaFacts);
		
		return creator.generateDatalogProgram();
	}
}