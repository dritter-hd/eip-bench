package com.eipbench.states;

import com.eipbench.generator.MessageSetLoader;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class Content {
    protected MessageSetLoader messageSet;

    @Param("1")
    protected int batchSize;
    
    @Param("-1")
    protected int msgScaleLevel;
    
    @Param("true")
    protected boolean offHeapMessages;

    @Param("none")
    protected String scaleName;

    @Param("none")
    protected String scaleUnit;
}