package com.eipbench.content;

import com.eipbench.camel.CustomerNationRegionEmbeddedMessageSet;

public class JavaCustomerNationRegionEmbeddedContent extends JavaContent {
    public JavaCustomerNationRegionEmbeddedContent() {
        super();
        messageSet = new CustomerNationRegionEmbeddedMessageSet();
    }
}
