package com.eipbench.states.datalog;

import com.eipbench.camel.DatalogCamelContent;
import com.eipbench.states.CamelFixture;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

public class DatalogFixture extends CamelFixture {
    private final DatalogCamelContent datalogCamelContent = new DatalogCamelContent();

    @Setup(Level.Trial)
    public void prepareContext() throws Exception {
        context.addRoutes(datalogCamelContent.getRouteContent());
        context.addRoutes(getBaselineRoute());
        context.start();
    }

    @TearDown(Level.Trial)
    public void shutdownContext() throws Exception {
        context.stop();
    }
}
