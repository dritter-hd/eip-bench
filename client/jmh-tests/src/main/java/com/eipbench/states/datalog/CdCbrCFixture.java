package com.eipbench.states.datalog;

import com.eipbench.Constants;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

public class CdCbrCFixture extends DatalogFixture {
    @Setup(Level.Trial)
    public void setup() throws Exception {
        endpoint = getEndpoint(Constants.TYPE.TPC_H_EIP_CD_CBR_C);
        producer = context.createProducerTemplate();
    }
}