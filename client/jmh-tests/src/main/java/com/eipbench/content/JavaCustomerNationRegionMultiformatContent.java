package com.eipbench.content;

import com.eipbench.camel.CustomerNationRegionMultiformatMessageSet;

public class JavaCustomerNationRegionMultiformatContent extends JavaContent {
    public JavaCustomerNationRegionMultiformatContent() {
        super();
        messageSet = new CustomerNationRegionMultiformatMessageSet();
    }
}
