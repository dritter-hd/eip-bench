package com.eipbench.benchmarks;

import com.eipbench.states.java.JavaBaseline;
import com.eipbench.content.JavaOrderContent;
import org.openjdk.jmh.annotations.Benchmark;

public class CjBl extends IntegrationPatternBenchmark {

    @Override
    public String getDisplayName() {
        return "Baseline Benchmark";
    }

    @Benchmark
    public void A(JavaOrderContent content, JavaBaseline fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

}
