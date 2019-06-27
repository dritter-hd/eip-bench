package com.eipbench;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;

import java.io.IOException;
import java.util.Collection;

public class SilentOutputFormat implements OutputFormat {
        public SilentOutputFormat() {
        }

        @Override
        public void print(String s) {
        }

        @Override
        public void println(String s) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void verbosePrintln(String s) {
        }

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void close() {
        }

        @Override
        public void iteration(BenchmarkParams benchParams, IterationParams params, int iteration) {
            
        }

        @Override
        public void iterationResult(BenchmarkParams benchParams, IterationParams params, int iteration, IterationResult data) {     
        }

        @Override
        public void startBenchmark(BenchmarkParams benchParams) {
        }

        @Override
        public void endBenchmark(BenchmarkResult result) {   
        }

        @Override
        public void startRun() {
        }

        @Override
        public void endRun(Collection<RunResult> result) {
        }
}
