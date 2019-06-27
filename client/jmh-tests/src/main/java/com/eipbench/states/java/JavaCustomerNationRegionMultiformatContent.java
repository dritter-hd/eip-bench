package com.eipbench.states.java;

import com.eipbench.camel.CustomerNationRegionMultiformatMessageSet;

public class JavaCustomerNationRegionMultiformatContent extends JavaContent {
    public JavaCustomerNationRegionMultiformatContent() {
        super();
        messageSet = new CustomerNationRegionMultiformatMessageSet();
    }
}
