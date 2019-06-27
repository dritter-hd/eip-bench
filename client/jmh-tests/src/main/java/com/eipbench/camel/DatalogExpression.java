package com.eipbench.camel;

import com.github.dritter.hd.dlog.algebra.ParameterValue;
import com.github.dritter.hd.dlog.evaluator.IFacts;
import com.github.dritter.hd.dlog.evaluator.SimpleFacts;
import org.apache.camel.Exchange;
import org.apache.camel.model.language.ExpressionDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class DatalogExpression extends ExpressionDefinition {
	private final String PROPERTY_PREFIX = "dlog-";
    
	public abstract <T> T evaluate(final Exchange exchange, Class<T> type);
//	public abstract DatalogProgram evaluate(DatalogProgram program);
	public abstract boolean matches(Exchange exchange);
	
	public void addPreviousEvaluationResultsToExchangeProperties(DatalogProgram oldProgram, Exchange exchange){
	    Collection<IFacts> facts = oldProgram.getFacts();
	    Collection<IFacts> metaFacts = oldProgram.getMetaFacts();	    	    
	    
	    for (IFacts fact : facts) {
           String predicate = fact.getPredicate();
           Collection<IFacts> factsToAdd = new ArrayList<IFacts>();
           factsToAdd.add(fact);
           DatalogProgram storedProgram = getStoredProgramFrom(exchange, predicate);
           
           Collection<IFacts> metaFactsToAdd = getRelevantMetaFactsFor(predicate, metaFacts);
           DatalogProgram datalogProgram = unionDatalogProgramAndFacts(storedProgram, factsToAdd, metaFactsToAdd);
           exchange.setProperty(PROPERTY_PREFIX+predicate, datalogProgram);
        }
	    
	    addRelationsWithoutFactsToProperties(exchange, metaFacts);
	}

    private DatalogProgram getStoredProgramFrom(Exchange exchange, String key) {
        DatalogProgram storedProgram = (DatalogProgram) exchange.getProperty(PROPERTY_PREFIX+key);
        if (storedProgram == null){
            storedProgram = DatalogProgram.create();
        }
        return storedProgram;
    }
	
    private DatalogProgram unionDatalogProgramAndFacts(DatalogProgram storedProgram, Collection<IFacts> factsToAdd,
            Collection<IFacts> metaFactsToAdd) {
        Collection<IFacts> storedFacts = storedProgram.getFacts();
           storedFacts.removeAll(factsToAdd);
           storedFacts.addAll(factsToAdd);
           
           Collection<IFacts> storedMetaFacts = storedProgram.getMetaFacts();
           storedMetaFacts.removeAll(metaFactsToAdd);
           storedMetaFacts.addAll(metaFactsToAdd);
           
           DatalogProgram datalogProgram = DatalogProgram.create(storedFacts, storedMetaFacts);
        return datalogProgram;
    }
	
    private Collection<IFacts> getRelevantMetaFactsFor(String predicate, Collection<IFacts> metaFacts) {
        List<List<ParameterValue<?>>> relevantMeatFacts = new ArrayList<List<ParameterValue<?>>>();
        for (IFacts iFacts : metaFacts) {
            for(List<ParameterValue<?>> parameterValues: iFacts.getValues()){
                if (parameterValues.get(0).toString().equals(predicate)){
                    relevantMeatFacts.add(parameterValues);
                }
            }
        }
        IFacts relevantMetaFacts = SimpleFacts.create(ConversionHelper.META_RELATION_NAME, 3, relevantMeatFacts);
        Collection<IFacts> relevantMetaFact = new ArrayList<IFacts>();
        relevantMetaFact.add(relevantMetaFacts);
        return relevantMetaFact;
    }
    
    private void addRelationsWithoutFactsToProperties(Exchange exchange, Collection<IFacts> metaFacts) {
        HashMap<String, List<List<ParameterValue<?>>>> missingRelations = checkMetaFactsForMissingRelations(exchange, metaFacts);
        
        for (String key : missingRelations.keySet()) {
            List<List<ParameterValue<?>>> values = missingRelations.get(key);
            IFacts metaFactToAdd = SimpleFacts.create(ConversionHelper.META_RELATION_NAME, 3, values);
            Collection<IFacts> metaFactsToAdd = new ArrayList<IFacts>();
            metaFactsToAdd.add(metaFactToAdd);
            
            Collection<IFacts> factsToAdd = new ArrayList<IFacts>();
            DatalogProgram datalogProgram = DatalogProgram.create(factsToAdd, metaFactsToAdd);
            exchange.setProperty(PROPERTY_PREFIX+key, datalogProgram);
        }
    }
    private HashMap<String, List<List<ParameterValue<?>>>> checkMetaFactsForMissingRelations(Exchange exchange, Collection<IFacts> metaFacts) {
        HashMap<String,List<List<ParameterValue<?>>>> metaFactMap = new HashMap<String,List<List<ParameterValue<?>>>>();
        for (IFacts iFacts : metaFacts){
            for(List<ParameterValue<?>> parameterValues: iFacts.getValues()){
                String predicateName = parameterValues.get(0).toString();
                if (exchange.getProperty(PROPERTY_PREFIX+predicateName) == null){
                    List<List<ParameterValue<?>>> list = metaFactMap.get(predicateName);
                    if (list == null){
                        list = new ArrayList<List<ParameterValue<?>>>();
                    }
                    list.add(parameterValues);
                    metaFactMap.put(predicateName, list);
                }
            }
        }
        return metaFactMap;
    }
}
