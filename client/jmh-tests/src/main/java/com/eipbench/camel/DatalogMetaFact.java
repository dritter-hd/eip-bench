package com.eipbench.camel;

import java.util.ArrayList;
import java.util.List;

public class DatalogMetaFact {
	private List<String> parameterNames = new ArrayList<String>();
	
	public void addParameter(String parameterName){
		parameterNames.add(parameterName);
	}
	
	public void removeParameter(String parameter){
		parameterNames.remove(parameter);
	}
	
	public String generateDatalogMetaFacts(String relationName){
		StringBuilder metaFact = new StringBuilder();
	
		int counter = 1;
		for (String parameterName : parameterNames) {
			metaFact.append(ConversionHelper.META_RELATION_NAME);
			metaFact.append(ConversionHelper.BEGIN_TOKEN);
			metaFact.append("\""+ConversionHelper.cleanString(relationName)+"\",");
			metaFact.append("\""+ConversionHelper.cleanString(parameterName)+"\",");
			metaFact.append(counter);
			metaFact.append(ConversionHelper.END_TOKEN);
			counter++;
		}
		return metaFact.toString();
	}
	
	public List<String> getParameterNameList(){
		return parameterNames;
	}
}
