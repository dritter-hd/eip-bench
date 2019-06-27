package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class OperatorBiggerEqualsThan implements Operator {
    private final String key;
    private final Number value;

    public OperatorBiggerEqualsThan(final String key, final Number value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean eval(final JsonNode node) {
        return node.get(key).asDouble() >= value.doubleValue();
    }
}
