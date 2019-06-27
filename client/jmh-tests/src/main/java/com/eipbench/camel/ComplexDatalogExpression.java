package com.eipbench.camel;

import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class ComplexDatalogExpression extends DatalogExpression {
	private List<DatalogRule> rules;

	public ComplexDatalogExpression(DatalogRule rule) {
		rules = new ArrayList<DatalogRule>();
		rules.add(rule);
	}

	public ComplexDatalogExpression(List<DatalogRule> rules) {
		this.rules = rules;
	}

	public <T> T evaluate(final Exchange exchange, Class<T> type) {
		DatalogProgram program = exchange.getIn().getBody(DatalogProgram.class);
		DatalogProgram result = evaluate(program);
		return exchange.getContext().getTypeConverter()
				.convertTo(type, result);
	}
	
	public DatalogProgram evaluate(DatalogProgram program){
		DatalogProgram result = DatalogHelper.evaluateRules(program, rules);
		return result;
	}

	@Override
	public boolean matches(Exchange exchange) {
		DatalogProgram program = exchange.getIn().getBody(DatalogProgram.class);
		DatalogProgram result = DatalogHelper.evaluateRules(program, rules);
		Boolean hasCorrespondingFacts = (result.getFacts().toString().length() > 2); //2, because there is always at least an empty element []
		return hasCorrespondingFacts;
	}

    @Override
    public String toString() {
        return "Rules: "+rules.toString();
    }

    public void setRules(List<DatalogRule> rules) {
        this.rules = rules;
    }
}