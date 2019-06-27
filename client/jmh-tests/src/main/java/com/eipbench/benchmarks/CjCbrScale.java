package com.eipbench.benchmarks;

import com.eipbench.states.java.CjCbrScaleAFixture;
import com.eipbench.states.java.JavaOrderContent;
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

    /*@Benchmark
    public void scale_no_A(JavaOrderContent content, CjCbrScaleNoAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void scale_B(JavaOrderContent content, CjCbrScaleBFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void scale_no_B(JavaOrderContent content, CjCbrScaleNoBFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void scale_C(JavaOrderContent content, CjCbrScaleCFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void scale_no_C(JavaOrderContent content, CjCbrScaleNoCFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void D(JavaCustomerNationRegionMultiformatContent content, CjCbrDFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void fm_D(JavaCustomerNationRegionMultiformatContent content, CjCbrFmDFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void E(JavaCustomerNationRegionEmbeddedContent content, CjCbrEFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }*/
}
