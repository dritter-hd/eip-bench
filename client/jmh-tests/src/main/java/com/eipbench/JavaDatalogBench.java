package com.eipbench;

import com.eipbench.benchmarks.CjBl;
import com.eipbench.benchmarks.IntegrationPatternBenchmark;
import com.eipbench.content.OrderMessageSet;
import com.eipbench.postprocessing.ChartGroup;
import com.eipbench.postprocessing.ResultBundle;
import com.eipbench.postprocessing.SilentOutputFormat;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.eipbench.pdgf.TpchDataGenerator;
import com.eipbench.tpchgenerator.TpchGenerateBenchData;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.BenchmarkListEntry;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class JavaDatalogBench {
    private static int JMH_FORK_DEBUG = 0;
    
    private static OptionsConfig throughputConfig = new OptionsConfig();

    private static OptionsConfig messageSizeScaleConfig = new OptionsConfig();

    public static int[] messageSizesInKB = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072,
            262144, 524288 };
    private static int[] msgScaleLevel = { 0, 1, 2, 3, 6, 8, 10, 12, 14, 16, 18 };

    private static final BenchmarkList list = BenchmarkList.defaultList();
    private static final IntegrationPatternBenchmark[] BenchmarkClasses = {new CjBl()};

    public static void main(String[] args) throws RunnerException {
        readArgumentsAsJson(args);
        final ResultBundle resultBundle = new ResultBundle("jdc");
        assertMessageSetExists();
        final OptionsBuilder base = getJvmOptions();

        final int forks = JMH_FORK_DEBUG;
        final int warmupIterations = 5;
        final int iterations = 5;

        throughputConfig.setForks(forks);
        throughputConfig.setWarmupIterations(warmupIterations);
        throughputConfig.setMeasurementIterations(iterations);

        final Options throughputOptions = throughputConfig.apply(new OptionsBuilder().parent(base.build())).exclude("Cj.*").build(); //.exclude("C(d.*|j(?!Ce))") //.exclude("C(d.*|j(?!Alo))").build();

        messageSizeScaleConfig.setForks(JMH_FORK_DEBUG);
        messageSizeScaleConfig.setWarmupIterations(forks);
        messageSizeScaleConfig.setMeasurementIterations(iterations);
        final Options messageSizeScaleOptions = messageSizeScaleConfig.apply(new OptionsBuilder().parent(base).parent(base.build())).build();

        printPredictedRuntime(throughputOptions);

        final Collection<ChartGroup> chartGroups = createChartGroups();
        messageSizeScaleConfig.setRun(false);
        if (messageSizeScaleConfig.isRun()) {
            measureMessageSize(msgScaleLevel, messageSizesInKB, resultBundle, messageSizeScaleOptions, chartGroups);
        }
        throughputConfig.setRun(true);
        if (throughputConfig.isRun()) {
            measureThroughput(resultBundle, throughputOptions, chartGroups);
        }
     }

    private static OptionsBuilder getJvmOptions() {
        final OptionsBuilder base = (OptionsBuilder) new OptionsBuilder().jvmArgs("-server", "-XX:+UseConcMarkSweepGC");

        boolean offHeapMessages = false;
        if (offHeapMessages) {
            base.jvmArgsAppend("-Xmx1500m", "-Xms1000m").param("offHeapMessages", "true");
        } else {
            base.jvmArgsAppend("-Xmx6000m", "-Xms3000m").param("offHeapMessages", "false");
        }
        return base;
    }

    private static void assertMessageSetExists() {
        if (!OrderMessageSet.MESSAGE_SET_FILE.exists()) {
            final TpchDataGenerator gen = new TpchDataGenerator();
            gen.prepareWorkingDir();
            gen.generate();
            try {
                System.out.println("Generating tpch bench data");
                TpchGenerateBenchData.main(new String[] {});
            } catch (final SQLException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readArgumentsAsJson(final String[] args) {
        if (args.length > 0) {
            try {
                System.out.println("reading config from JSON in args[0]");
                System.out.println(args[0]);
                final JSONObject masterNode = new JSONObject(args[0]);
                loadJsonConfig(masterNode);
                printJsonConfig();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static Collection<ChartGroup> createChartGroups() {
        final ArrayList<ChartGroup> chartGroups = new ArrayList<>();
        chartGroups.add(new ChartGroup(series -> true, "chart-all", "All Variants"));

        final HashMap<String, Boolean> onlyOnceMap = new HashMap<>();

        for (final IntegrationPatternBenchmark benchmark : BenchmarkClasses) {
            final String type = benchmark.getBenchmarkType();
            if (!onlyOnceMap.containsKey(type)) {
                onlyOnceMap.put(type, true);
                chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)" + type + "(.*)$").asPredicate(), benchmark.getFileName(), benchmark
                        .getDisplayName()));
            }
        }
        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)(.*)no(.*)$").asPredicate(), "chart-no", "All with no Optimisation"));
        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)(.*)np(.*)$").asPredicate(), "chart-np", "All with no Parallelisation"));
        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)(.*)om(.*)$").asPredicate(), "chart-om", "All with Original Message"));

        chartGroups.add(new ChartGroup(Pattern.compile("(CjCbr\\.no(.*)$)|(CdCbr(.*)$)").asPredicate(), "chart-cbr-no",
                "Content-based Router/Unoptimized Java"));
        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)Cbr\\.(?<!no)(.*)$").asPredicate(), "chart-cbr-optimised",
                "Content-based Router optimised"));
        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)(Mc|Rl)(.*)$").asPredicate(), "chart-mf-rl", "Multicast/Recipient List"));
        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)(Mf|Cbr)(.*)$").asPredicate(), "chart-mf-cbr",
                "Message Filter/Content-based Router"));

        chartGroups.add(new ChartGroup(Pattern.compile("C(j|d)(Alo|Eo|Eoio|Id|Rs)(.*)$").asPredicate(), "chart-qos",
                "Quality of Service Pattern"));
        return chartGroups;
    }

    private static void printPredictedRuntime(Options throughputOptions) {
        final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        time.setTimeZone(TimeZone.getTimeZone("UTC"));

        long throughputRun = 0;
        if (throughputConfig.isRun()) {
            throughputRun = predictTime(throughputConfig, throughputOptions);
            System.out.println("Throughput Run: " + time.format(new Date(throughputRun)));
        }

        System.out.println("Total: " + time.format(new Date(throughputRun)));
    }

    private static void measureThroughput(final ResultBundle resultBundle, final Options throughputOptions, final Collection<ChartGroup> chartGroups)
            throws RunnerException {
        String id = "throughput";
        final File measurementFolder = resultBundle.createMeasurement(id);
        System.out.println(throughputOptions.getExcludes());
        final Collection<RunResult> results = new Runner(throughputOptions).run();
        writeResultObject(measurementFolder, results);
    }

    private static int predictTime(final OptionsConfig config, final Options option) {
        final SortedSet<BenchmarkListEntry> benchmarks = list.find(new SilentOutputFormat(), config.getInclude(), config.getExclude());
        final int numberOfTests = benchmarks.size();
        System.out.println("Benchmarks (" + numberOfTests + ") : " + benchmarks);
        int forkCount = option.getForkCount().get();
        if (forkCount == 0) {
            forkCount = 1;
        }
        final int parsingTime = 1;
        final int millisPerIteration = 1000;
        return millisPerIteration * (option.getWarmupIterations().get() + option.getMeasurementIterations().get() + parsingTime)
                * forkCount * numberOfTests;
    }

    private static void measureBatchScaleThroughput(final int[] batchSizes, final ResultBundle resultBundle, final Options base,
            final Collection<ChartGroup> chartGroups) throws RunnerException {
        final String id = "batch-scale-throughput";
        final File measurementFolder = resultBundle.createMeasurement(id);

        final Map<Integer, Collection<RunResult>> scaleResults = new LinkedHashMap<>();
        for (final int batchSize : batchSizes) {
            final OptionsBuilder opt = new OptionsBuilder();
            opt.operationsPerInvocation(batchSize).parent(base).param("batchSize", String.valueOf(batchSize)).exclude("C(d(?!Cbr)|j(?!Cbr))")
                    .param("scaleName", "Batch Size").param("scaleUnit", "msg/batch");
            final Collection<RunResult> scale = new Runner(opt.build()).run();
            scaleResults.put(batchSize, scale);
        }
        writeResultObject(measurementFolder, scaleResults);
    }
    
    private static void measureMessageSize(final int[] msgScaleLevel, final int[] messageSizesInKB, final ResultBundle resultBundle, final Options base,
            final Collection<ChartGroup> chartGroups) throws RunnerException {
        final String id = "message-size-throughput";
        final File measurementFolder = resultBundle.createMeasurement(id);

        final Map<Integer, Collection<RunResult>> scaleResults = new LinkedHashMap<>();
        for (final int scaleLevel : msgScaleLevel) {
            final OptionsBuilder opt = new OptionsBuilder();
            opt.parent(base).param("msgScaleLevel", String.valueOf(scaleLevel)).exclude("C(d(?!CbrScale)|j(?!CbrScale))")//.exclude("C(d(?!CbrScale)|j.*)")//.exclude("C(d.*|j(?!Cbr))") // FIXME: limit only to scaling tests
                    .param("scaleName", "Message Size").param("scaleUnit", "kb");
            final Collection<RunResult> scale = new Runner(opt.build()).run();
            scaleResults.put(scaleLevel, scale);
        }
        writeResultObject(measurementFolder, scaleResults);
    }

    private static void measureThreadScaleThroughput(final int[] threads, final ResultBundle resultBundle, final Options base,
            final Collection<ChartGroup> chartGroups) throws RunnerException {
        final String id = "batch-thread-throughput";
        final File measurementFolder = resultBundle.createMeasurement(id);

        final Map<Integer, Collection<RunResult>> scaleResults = new LinkedHashMap<>();
        for (final int threadCount : threads) {
            final OptionsBuilder opt = (OptionsBuilder) new OptionsBuilder().parent(base).threads(threadCount).exclude("C(d.*|j(?!Cbr))") //.exclude("cd_(.*)$")
                    .param("scaleName", "Threads").param("scaleUnit", "#");
            final Collection<RunResult> scale = new Runner(opt.build()).run();
            scaleResults.put(threadCount, scale);
        }
        writeResultObject(measurementFolder, scaleResults);
    }

    private static void writeResultObject(final File folder, final Object result) {
        final Kryo kryo = new Kryo();
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        try {
            final Output output = new Output(new FileOutputStream(new File(folder, "raw.dat")));
            kryo.writeObject(output, result);
            output.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private static void loadJsonConfig(final JSONObject masterNode) {
        try {
            final JSONObject throughputNode = masterNode.getJSONObject("throughput");
            final JSONObject batchScaleNode = masterNode.getJSONObject("batchScale");
            final JSONObject threadScaleNode = masterNode.getJSONObject("threadScale");
            final JSONArray batchArrayNode = masterNode.getJSONArray("batchSizes");
            final JSONArray threadArrayNode = masterNode.getJSONArray("threads");
            
            throughputConfig.loadJsonConfig(throughputNode);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private static void printJsonConfig() {
        System.out.println("benchmark options");

        throughputConfig.printConfig("throughputConfig.");
    }
}
