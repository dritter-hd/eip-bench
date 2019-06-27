package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class TpcRouterNotQueryOptimized extends TpcRouterQueryOptimized {
    public TpcRouterNotQueryOptimized(String path, Operator... operator) {
        super(path, operator);
    }

    public TpcRouterNotQueryOptimized(String path, String key, String value) {
        super(path, key, value);
    }

    @Override
    // switched result
    protected boolean matches(JsonNode objectNode) {
        for (Operator operator : operators) {
            if (!operator.eval(objectNode)) {
                return true;
            }
        }
        return false;
    }
}