package com.eipbench.camel;

import org.apache.camel.Exchange;

public class DatalogRoutingNotExpression extends DatalogRoutingExpression {
    public DatalogRoutingNotExpression(String rule) {
        super(rule);
    }

    @Override
    public <T> T evaluate(final Exchange exchange, final Class<T> type) {
        T result = super.evaluate(exchange, type);
        return (T) ((result == Boolean.TRUE) ? Boolean.FALSE : Boolean.TRUE);
    }
}
