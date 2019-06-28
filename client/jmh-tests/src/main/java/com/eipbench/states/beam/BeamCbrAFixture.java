package com.eipbench.states.beam;

import com.eipbench.content.Constants;
import com.eipbench.states.java.JavaFixture;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

public class BeamCbrAFixture extends StreamFixture {
    @Setup(Level.Trial)
    public void setup() throws Exception {
        endpoint = getEndpoint(Constants.TYPE.TPC_H_EIP_BEAM_CBR_A);
        producer = context.createProducerTemplate();
    }
}