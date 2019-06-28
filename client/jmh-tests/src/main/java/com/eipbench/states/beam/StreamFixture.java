package com.eipbench.states.beam;

import com.eipbench.camel.JavaCamelContent;
import com.eipbench.states.BeamFixture;
import com.eipbench.states.CamelFixture;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

public class StreamFixture extends BeamFixture {
    private final JavaCamelContent javaCamelContent = new JavaCamelContent();

    @Setup(Level.Trial)
    public void prepareContext() throws Exception {
        context.addRoutes(javaCamelContent.getRouteContent());
        context.addRoutes(getBaselineRoute());
        context.start();
    }

    @TearDown(Level.Trial)
    public void shutdownContext() throws Exception {
        context.stop();
    }
}
