package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class TpcRouterNotQuery extends TpcRouterQueryOptimized {
    public TpcRouterNotQuery(String path, Operator... operator) {
        super(path, operator);
    }

    public TpcRouterNotQuery(String path, String key, String value) {
        super(path, key, value);
    }

    @Override
    // switched result
    protected boolean matches(JsonNode objectNode) {
        boolean matches = true;
        for (Operator operator : operators) {
            if (operator.eval(objectNode)) {
                matches = false;
            }
        }
        return matches;
    }
}