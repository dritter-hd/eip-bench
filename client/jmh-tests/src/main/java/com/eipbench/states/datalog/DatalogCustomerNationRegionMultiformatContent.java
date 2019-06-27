package com.eipbench.states.datalog;

import com.eipbench.camel.CustomerNationRegionMultiformatMessageSet;

public class DatalogCustomerNationRegionMultiformatContent extends DatalogContent {
    public DatalogCustomerNationRegionMultiformatContent() {
        super();
        messageSet = new CustomerNationRegionMultiformatMessageSet();
    }
}
