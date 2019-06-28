package com.eipbench.states.java;

import com.eipbench.content.Constants;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

public class CjCbrFmDFixture extends JavaFixture {
    @Setup(Level.Trial)
    public void setup() throws Exception {
        endpoint = getEndpoint(Constants.TYPE.TPC_H_EIP_CJ_CBR_FM_D);
        producer = context.createProducerTemplate();
    }
}

