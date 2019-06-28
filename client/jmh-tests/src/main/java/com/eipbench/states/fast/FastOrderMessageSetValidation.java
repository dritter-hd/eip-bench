package com.eipbench.states.fast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.eipbench.pdgf.TpchDataGenerator;
import com.eipbench.postprocessing.ResultBundle;
import com.eipbench.states.java.CjCbrAFixture;
import com.eipbench.content.JavaOrderContent;
import com.eipbench.tpchgenerator.TpchGenerateBenchData;
import com.eipbench.content.OrderMessageSet;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

public class FastOrderMessageSetValidation {

    //@Benchmark
    public void java1A_slow(JavaOrderContent content, CjCbrAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    //@Benchmark
    public void java1A_mmap(FastJavaContent content, CjCbrAFixture fixture) {
        fixture.getProducer().sendBody(fixture.getEndpoint(), content.getMessage());
    }

    public static void main(String[] args) throws RunnerException {
        if (!OrderMessageSet.MESSAGE_SET_FILE.exists()) {
            final TpchDataGenerator gen = new TpchDataGenerator();
                gen.prepareWorkingDir();
                gen.generate();
            try {
                System.out.println("Generating tpch bench data");
                TpchGenerateBenchData.main(new String[]{});
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Options base = (OptionsBuilder)new OptionsBuilder()
                .include(FastOrderMessageSetValidation.class.getSimpleName())
                .jvmArgs("-Xmx6000m", "-Xms3000m", "-server", "-XX:+UseConcMarkSweepGC")
                .exclude("(.*)mmap")
                .warmupIterations(5)
                .measurementIterations(30)
                .forks(10)
                .build();
        Collection<RunResult> results = new Runner(base).run();

        Options fast = (OptionsBuilder)new OptionsBuilder()
                .include(FastOrderMessageSetValidation.class.getSimpleName())
                .jvmArgs("-Xmx1000m", "-Xms1000m", "-server", "-XX:+UseConcMarkSweepGC")
                .exclude("(.*)slow")
                .warmupIterations(5)
                .measurementIterations(30)
                .forks(10)
                .build();
        Collection<RunResult> fastResults = new Runner(fast).run();
    }

    private static void measureThroughput(ResultBundle resultBundle, Options throughputOptions) throws RunnerException {
        String id = "throughput";
        File measurementFolder = resultBundle.createMeasurement(id);
        Collection<RunResult> results = new Runner(throughputOptions).run();
        /*writeResultObject(measurementFolder, results);
        ThroughputPostProcessing postProcessing = new ThroughputPostProcessing();
        postProcessing.process(measurementFolder, results);*/
    }

    private static int predictTime(int numberOfTests, Options option) {
        return 1000 /* millis per iteration */ * (option.getWarmupIterations().get() + option.getMeasurementIterations().get() + 20 /* parsing time */) * option.getForkCount().get() * numberOfTests;
    }

    private static void writeResultObject(File folder, Object result) {
        Kryo kryo = new Kryo();
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        try {
            Output output = new Output(new FileOutputStream(new File(folder, "raw.dat")));
            kryo.writeObject(output, result);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
