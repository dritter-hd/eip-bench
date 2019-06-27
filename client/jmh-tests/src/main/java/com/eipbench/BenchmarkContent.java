package com.eipbench;

import org.apache.camel.builder.RouteBuilder;

public interface BenchmarkContent {

    public abstract RouteBuilder getRouteContent() throws Exception;

}