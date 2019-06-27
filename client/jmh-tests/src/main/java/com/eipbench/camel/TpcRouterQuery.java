package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class TpcRouterQuery  extends TpcRouterQueryOptimized {
    public TpcRouterQuery(String path, Operator... operator) {
        super(path, operator);
    }

    public TpcRouterQuery(String path, String key, String value) {
        super(path, key, value);
    }

    @Override
    protected boolean matches(JsonNode objectNode) {
        boolean matches = false;
        for (Operator operator : operators) {
            if (operator.eval(objectNode)) {
                matches = true;
            }
        }
        return matches;
    }
}