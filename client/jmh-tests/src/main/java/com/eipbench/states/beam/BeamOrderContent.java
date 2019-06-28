package com.eipbench.states.beam;

import com.eipbench.content.OrderMessageSet;
import com.eipbench.states.datalog.DatalogContent;

public class BeamOrderContent extends BeamContent {
    public BeamOrderContent() {
        super();
        messageSet = new OrderMessageSet();
    }
}
