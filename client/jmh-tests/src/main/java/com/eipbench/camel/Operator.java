package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public interface Operator {
    public boolean eval(JsonNode node);
}
