package com.eipbench.camel;


import com.github.dritter.hd.dlog.algebra.ParameterValue;
import com.github.dritter.hd.dlog.evaluator.DlogEvaluator;
import com.github.dritter.hd.dlog.evaluator.IFacts;
import com.github.dritter.hd.dlog.parser.DlogEvaluatorParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DatalogHelper {

	public static RuleBody getParameterNamesFromMetaFactsFor(String predicate,
			final DatalogProgram datalogProgram) {
		String rule = predicate
				+ "Meta(relName, attrName, attrPos) :- meta(relName, attrName, attrPos).";

		DlogEvaluator metaEvaluator = DlogEvaluator.create();
		metaEvaluator.initalize(datalogProgram.getMetaFacts(), rule);

		IFacts correspondingMetafacts = metaEvaluator.query(predicate + "Meta",
				3, new String[] { predicate, null, null });

		RuleBody ruleBody = new RuleBody(correspondingMetafacts.getValues()
				.size());
		for (List<ParameterValue<?>> facts : correspondingMetafacts.getValues()) {
			ruleBody.insertAttribute((Integer) facts.get(2).get(),
					(String) facts.get(1).get());
		}
		return ruleBody;
	}

	public static DatalogProgram evaluateRules(DatalogProgram datalogProgram,
			List<DatalogRule> rules) {
		DlogEvaluator evaluator = DlogEvaluator.create();
		String ruleString = buildRuleString(rules);
		evaluator.initalize(datalogProgram.getFacts(), ruleString);
		Collection<IFacts> correspondingFacts = convertParserFactsToEvaluatorFacts(evaluator
				.evaluateRules());
		
		return DatalogProgram.create(correspondingFacts, generateMetaFactsFromRules(rules));
	}
	
	private static Collection<IFacts> generateMetaFactsFromRules(List<DatalogRule> rules){
		DatalogMetaFacts metaFacts = new DatalogMetaFacts(); 
		
		for (DatalogRule datalogRule : rules) {
			DatalogMetaFact metaFact = constructMetaFactFromRule(datalogRule);
			metaFacts.add(datalogRule.getPredicateName(), metaFact);
		}

		DlogEvaluatorParser parser = DlogEvaluatorParser.create();
		parser.parse(metaFacts.generateDatalogFacts());
		return parser.getFacts();
	}

    private static DatalogMetaFact constructMetaFactFromRule(DatalogRule datalogRule) {
        String[] parameterNames = datalogRule.getArguments().split(",");
        DatalogMetaFact metaFact = new DatalogMetaFact();
        for (String parameter : parameterNames) {
        	metaFact.addParameter(parameter);
        }
        return metaFact;
    }
	
	public static DatalogProgram query(DatalogProgram datalogProgram,
			List<DatalogRule> rules, DatalogQuery query) {
		DlogEvaluator evaluator = DlogEvaluator.create();
		
		if (rules != null && !rules.isEmpty()){
		String ruleString = buildRuleString(rules);
		evaluator.initalize(datalogProgram.getFacts(), ruleString);
		} else {
			evaluator.initalize(datalogProgram.getFacts(), "");
		}
		IFacts queryResult = evaluator.query(query.getPredicateName(), query.getArity(), query.getParams());
		Collection<IFacts> correspondingFacts = new ArrayList<IFacts>();
		correspondingFacts.add(queryResult);
		
		DatalogProgram result = DatalogProgram.create(correspondingFacts, getMeatFactsFor(query,rules,datalogProgram.getMetaFacts()));
		return result;
	}

	private static Collection<IFacts> getMeatFactsFor(DatalogQuery query, List<DatalogRule> rules, Collection<IFacts> metaFacts) {
	    for (IFacts iFacts : metaFacts) {
            if (iFacts.getPredicate().equals(query.getPredicateName())){
                Collection<IFacts> queryMetaFacts = new ArrayList<IFacts>();
                queryMetaFacts.add(iFacts);
                return queryMetaFacts;
            }
        }
	    
	    for (DatalogRule datalogRule : rules){
	        if (datalogRule.getPredicateName().equals(query.getPredicateName())){
	            DatalogMetaFact metaFact = constructMetaFactFromRule(datalogRule);
	            DlogEvaluatorParser parser = DlogEvaluatorParser.create();
	            parser.parse(metaFact.generateDatalogMetaFacts(datalogRule.getPredicateName()));
	            return parser.getFacts();
	        }
	    }
        
	    return new ArrayList<IFacts>();
    }

    private static String buildRuleString(List<DatalogRule> rules) {
		StringBuilder ruleString = new StringBuilder();

		//FIXME: if rule contains attributes with unallowed characters, they have to be remove e.g. _
		
		for (DatalogRule datalogRule : rules) {
			ruleString.append(datalogRule.toString());
		}
		return ruleString.toString();
	}

	private static Collection<IFacts> convertParserFactsToEvaluatorFacts(
			Collection<com.github.dritter.hd.dlog.IFacts> parserFacts) {
		StringBuffer facts = new StringBuffer();
		for (com.github.dritter.hd.dlog.IFacts iFacts : parserFacts) {
			facts.append(iFacts.toString());
		}
		DlogEvaluatorParser evaluatorParser = DlogEvaluatorParser.create();
		evaluatorParser.parse(facts.toString());
		return evaluatorParser.getFacts();
	}

    public static DatalogProgram query(DatalogProgram program, DatalogQuery query) {
        return query(program, new ArrayList<DatalogRule>(), query);
    }

}
