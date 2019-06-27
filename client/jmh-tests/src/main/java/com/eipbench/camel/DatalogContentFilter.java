package com.eipbench.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatalogContentFilter implements Processor {
    
    private static final transient Logger LOG = LoggerFactory.getLogger(DatalogContentFilter.class);

	private DatalogExpression expression;

	public DatalogContentFilter(DatalogExpression expression) {
		this.expression = expression;
	}

	public void process(final Exchange exchange) throws Exception {
	    LOG.info("Executing expression "+expression.toString());
		DatalogProgram oldProgram = exchange.getIn().getBody(DatalogProgram.class);
	    DatalogProgram result = expression.evaluate(exchange, DatalogProgram.class);
		exchange.getIn().setBody(result);
		expression.addPreviousEvaluationResultsToExchangeProperties(oldProgram, exchange);
	}

    @Override
    public String toString() {
        return "DatalogContentFilter: "+expression.toString();
    }
}
