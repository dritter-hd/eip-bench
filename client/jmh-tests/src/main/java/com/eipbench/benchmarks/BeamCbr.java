package com.eipbench.benchmarks;

import com.eipbench.states.beam.BeamCbrAFixture;
import com.eipbench.states.beam.BeamOrderContent;
import org.openjdk.jmh.annotations.Benchmark;

public class BeamCbr extends IntegrationPatternBenchmark {

    @Override
    public String getDisplayName() {
        return "Content-based Router";
    }

    @Benchmark
    public void A(BeamOrderContent content, BeamCbrAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }
}
