package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class OperatorArrayList implements Operator {
    private final int index;
    private final Operator operator;

    public OperatorArrayList(int index, Operator operator) {
        this.index = index;
        this.operator = operator;
    }

    @Override
    public boolean eval(JsonNode node) {
        ArrayNode arraynode = (ArrayNode) node;
        JsonNode indexnode = arraynode.get(index);
        return  operator.eval(indexnode);
    }
}
