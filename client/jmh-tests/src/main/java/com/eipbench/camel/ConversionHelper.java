package com.eipbench.camel;

import com.github.dritter.hd.dlog.evaluator.IFacts;
import com.github.dritter.hd.dlog.parser.DlogEvaluatorParser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConversionHelper {

	public static final String META_RELATION_NAME = "meta";
	public static final String BEGIN_TOKEN = "(";
	public static final String END_TOKEN = ").";
	public static final String PARENT_ID_NAME = "parentId";
	public static final String UID_NAME = "uid";

	public static String cleanString(String string) {
		String cleanedString = string.replaceAll("[^a-zA-Z0-9 -]", "").replaceAll("_", "");
		return cleanedString;
	};

	public static Collection<IFacts> parse(String facts) {
		DlogEvaluatorParser factParser = DlogEvaluatorParser.create();
		factParser.parse(facts);
		return factParser.getFacts();
	}
	
	public static DatalogProgram createDatalogProgramFromNamedDatalogFactList(List<NamedDatalogFact> relations) throws UnsupportedDataTypeException {
		DatalogProgramCreator programCreator = new DatalogProgramCreator();

		DatalogFacts facts = programCreator.getFacts();
		DatalogMetaFacts metaFacts = programCreator.getMetaFacts();

		for (NamedDatalogFact relation : relations) {
			String name = relation.getName();

			if (metaFacts.haveFactsFor(name)) {
				// create DatalogRule according to already existent
				// metaFactEntries
				DatalogFact fact = new DatalogFact();
				DatalogMetaFact metaFact = metaFacts.getFactsForRelation(name);
				Map<String, Object> currentParametersAndValues = relation
						.getParameters();
				for (String parameter : metaFact.getParameterNameList()) {
					if (currentParametersAndValues.containsKey(parameter)) {
						// Parameter already in metaFacts
						fact.insertParamater(
								currentParametersAndValues.get(parameter));
						currentParametersAndValues.remove(parameter);
					} else {
						fact.insertParamater("null");
					}
				}

				// while currentParametersAndValues still have more values
				// create additional parameters for the relation
				for (Entry<String, Object> parameter : currentParametersAndValues
						.entrySet()) {
					fact.insertParamater(parameter.getValue());
					metaFact.addParameter(parameter.getKey());
				}

				facts.add(name, fact);
				metaFacts.add(name, metaFact);
			} else {
				Map<String, Object> currentParametersAndValues = relation
						.getParameters();
				DatalogFact fact = new DatalogFact();
				DatalogMetaFact metaFact = new DatalogMetaFact();
				for (Entry<String, Object> parameter : currentParametersAndValues
						.entrySet()) {
					metaFact.addParameter(parameter.getKey());
					fact.insertParamater(parameter.getValue());
				}

				facts.add(name, fact);
				metaFacts.add(name, metaFact);

			}
		}

		DatalogProgram generateDatalogProgram = programCreator
				.generateDatalogProgram();
		return generateDatalogProgram;
	}
}