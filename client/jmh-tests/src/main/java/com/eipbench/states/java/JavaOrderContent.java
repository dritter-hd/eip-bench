package com.eipbench.states.java;

import com.eipbench.generator.OrderMessageSet;

public class JavaOrderContent extends JavaContent {
    public JavaOrderContent() {
        super();
        messageSet = new OrderMessageSet();
    }
}
