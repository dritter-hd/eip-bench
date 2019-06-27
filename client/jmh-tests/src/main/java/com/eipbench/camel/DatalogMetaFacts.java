package com.eipbench.camel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DatalogMetaFacts {
	
	private HashMap<String,DatalogMetaFact> facts = new HashMap<String, DatalogMetaFact>();
	
	public DatalogMetaFact getFactsForRelation(String relationName){
		return facts.get(relationName);
	}
	
	public String generateDatalogFacts(){
		StringBuilder factSb = new StringBuilder();
		for (Entry<String, DatalogMetaFact> factEntries : facts.entrySet()) {
			String relationName = factEntries.getKey();
			DatalogMetaFact fact = factEntries.getValue();
			factSb.append(fact.generateDatalogMetaFacts(relationName));
		}
		return factSb.toString();
	}
	
	public boolean haveFactsFor(String relationName){
		return facts.containsKey(relationName);
	}

	public void add(String name, DatalogMetaFact metaFact) {
		facts.put(name, metaFact);
	}
	
	public String getStringRepresentationForSave(){
		StringBuilder factSb = new StringBuilder();
		for (Entry<String, DatalogMetaFact> factEntries : facts.entrySet()) {
			factSb.append(factEntries.getKey()+"=");
			for (String parameterName : factEntries.getValue().getParameterNameList()){
				factSb.append(parameterName+",");
			}
			//delete trailing comma
			factSb.deleteCharAt(factSb.length()-1);
			factSb.append(System.getProperty("line.separator"));
		}
		return factSb.toString();
	}
	
	public Map<String, DatalogMetaFact> getMetaFacts(){
	    return facts;
	}
	
	public int getSize(){
	    return facts.size();
	}
}
