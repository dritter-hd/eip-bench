package com.eipbench.benchmarks;

import com.eipbench.states.datalog.*;
import org.openjdk.jmh.annotations.Benchmark;

public class CdCbr extends IntegrationPatternBenchmark {

    @Override
    public String getDisplayName() {
        return "Content-based Router";
    }

    @Benchmark
    public void A(DatalogOrderContent content, CdCbrAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void B(DatalogOrderContent content, CdCbrBFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void C(DatalogOrderContent content, CdCbrCFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void D(DatalogCustomerNationRegionMultiformatContent content, CdCbrDFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

}
