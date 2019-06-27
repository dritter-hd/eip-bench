package com.eipbench.camel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class DatalogFacts {
	
	private HashMap<String,List<DatalogFact>> facts = new HashMap<String, List<DatalogFact>>();
	
	public List<DatalogFact> getFactsForRelation(String relationName){
		return facts.get(relationName);
	}
	
	public String generateDatalogFactsUsing(DatalogMetaFacts metafacts) throws UnsupportedDataTypeException{
		StringBuilder factSb = new StringBuilder();
 		for (Entry<String, List<DatalogFact>> factEntries : facts.entrySet()) {
			String relationName = factEntries.getKey();
			DatalogMetaFact metaFact = metafacts.getFactsForRelation(relationName);
			for (DatalogFact fact : factEntries.getValue()) {
				factSb.append(generateDatalogFact(relationName, fact, metaFact));
			}
		}
		return factSb.toString();
	}

	private String generateDatalogFact(String relationName, DatalogFact fact, DatalogMetaFact metaFact) throws UnsupportedDataTypeException {
		List<Object> factParameters = fact.getParameters();
		List<String> parameterNames = metaFact.getParameterNameList();
		
		StringBuilder factSb = new StringBuilder();
		factSb.append(ConversionHelper.cleanString(relationName));
		factSb.append(ConversionHelper.BEGIN_TOKEN);
		factSb.append(fact.generateDatalogFactBody());
		
		int amountOfNullsToBeAdded = parameterNames.size() - factParameters.size();
		for (int i = 0; i < amountOfNullsToBeAdded; i++) {
			factSb.append(",\"null\"");
		}
		
		factSb.append(ConversionHelper.END_TOKEN);
		return factSb.toString();
	}

	public void add(String name, DatalogFact fact) {
		if (facts.containsKey(name)){
			List<DatalogFact> factList = facts.get(name);
			factList.add(fact);
			facts.put(name, factList);
		} else {
			List<DatalogFact> factList = new ArrayList<DatalogFact>();
			factList.add(fact);
			facts.put(name, factList);
		}
	}
}
