package com.eipbench.states.datalog;

import com.eipbench.content.OrderMessageSet;

public class DatalogOrderContent extends DatalogContent {
    public DatalogOrderContent() {
        super();
        messageSet = new OrderMessageSet();
    }
}
