package com.eipbench.states.java;

import com.eipbench.camel.CustomerNationRegionEmbeddedMessageSet;

public class JavaCustomerNationRegionEmbeddedContent extends JavaContent {
    public JavaCustomerNationRegionEmbeddedContent() {
        super();
        messageSet = new CustomerNationRegionEmbeddedMessageSet();
    }
}
