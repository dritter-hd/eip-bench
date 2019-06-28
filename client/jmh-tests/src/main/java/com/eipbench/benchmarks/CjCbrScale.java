package com.eipbench.benchmarks;

import com.eipbench.states.java.CjCbrScaleAFixture;
import com.eipbench.content.JavaOrderContent;
import org.openjdk.jmh.annotations.Benchmark;

public class CjCbrScale extends IntegrationPatternBenchmark {

    @Override
    public String getDisplayName() {
        return "Content-based Router (msg size scaling)";
    }

    @Benchmark
    public void scale_A(final JavaOrderContent content, final CjCbrScaleAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }
}
