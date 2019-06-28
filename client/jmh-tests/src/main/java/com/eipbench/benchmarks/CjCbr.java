package com.eipbench.benchmarks;

import com.eipbench.content.JavaCustomerNationRegionEmbeddedContent;
import com.eipbench.content.JavaCustomerNationRegionMultiformatContent;
import com.eipbench.content.JavaOrderContent;
import com.eipbench.states.java.*;
import org.openjdk.jmh.annotations.Benchmark;

public class CjCbr extends IntegrationPatternBenchmark {

    @Override
    public String getDisplayName() {
        return "Content-based Router";
    }

    @Benchmark
    public void A(JavaOrderContent content, CjCbrAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void no_A(JavaOrderContent content, CjCbrNoAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    /*@Benchmark
    public void scale_A(final JavaOrderContent content, final CjCbrScaleAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }*/
    
    @Benchmark
    public void B(JavaOrderContent content, CjCbrBFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void no_B(JavaOrderContent content, CjCbrNoBFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void C(JavaOrderContent content, CjCbrCFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    @Benchmark
    public void no_C(JavaOrderContent content, CjCbrNoCFixture fixture) {
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
    }
}
