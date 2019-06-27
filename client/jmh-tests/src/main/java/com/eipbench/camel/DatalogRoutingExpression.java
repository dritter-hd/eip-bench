package com.eipbench.camel;

import com.github.dritter.hd.dlog.IFacts;
import com.github.dritter.hd.dlog.algebra.DataIterator;
import com.github.dritter.hd.dlog.evaluator.DlogEvaluator;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;

import java.util.Collection;
import java.util.List;

public class DatalogRoutingExpression implements Expression {
    private static final Object syncronisedObjectLock = new Object();
    private final String rule;
    protected final DlogEvaluator evaluator;

    public DatalogRoutingExpression(final String rule) {
        this.rule = rule;
        evaluator = DlogEvaluator.create(rule);
    }

    @Override
    public <T> T evaluate(final Exchange exchange, final Class<T> type) {
        synchronized(syncronisedObjectLock){
            final List<IFacts> oldFacts = exchange.getIn().getBody(List.class);
            evaluator._initalize(oldFacts);
            final Collection<com.github.dritter.hd.dlog.IFacts> result = evaluator.evaluateNonRecursiveRules();

            final DataIterator values = result.iterator().next().getValues();
            values.open();
            return (T) ((values.next() != null) ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    @Override
    public String toString() {
        return rule;
    }
}
