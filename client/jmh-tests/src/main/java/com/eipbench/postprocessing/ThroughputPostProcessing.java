package com.eipbench.postprocessing;

import com.curiousdev.xtchart.Chart;
import com.curiousdev.xtchart.CompoundChart;
import com.curiousdev.xtchart.StyleManager;
import gnu.trove.list.array.TDoubleArrayList;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormat;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ThroughputPostProcessing extends BasicPostProcessing {

    public void process(File measurementFolder, Collection<RunResult> results, Collection<ChartGroup> chartGroups, SeriesSorting sorting) {
        writeRawResultTextFile(measurementFolder, results);

        Chart chart = createChart("");
        Chart finalChart = createFinalChart("");

        boolean containsOffHeapOnHeapVariants = checkContainsOffHeapOnHeapVariants(results);
        ArrayList<SeriesBundle> allData = new ArrayList();

        for (RunResult runResult : results) {
            String name = runResult.getParams().getBenchmark();
            String benchmarkName = name.substring(name.lastIndexOf(".",name.lastIndexOf(".")-1) + 1);
            if (containsOffHeapOnHeapVariants) {
                benchmarkName = benchmarkName + (Boolean.parseBoolean(runResult.getParams().getParam("offHeapMessages")) ? "-offheap" : "");
            }

            TDoubleArrayList yData = new TDoubleArrayList();
            TDoubleArrayList xData = new TDoubleArrayList();
            SeriesBundle currentSeries = new SeriesBundle(benchmarkName,xData,yData,runResult);

            int counter = 0;

            List<List<IterationResult>> iterationResults = new ArrayList<>();

            for (int i = 0; i < runResult.getParams().getMeasurement().getCount(); i++) {
                iterationResults.add(new ArrayList<>(runResult.getParams().getForks()));
            }

            /* Computing the aggregates over the fork runs */
            for (BenchmarkResult benchmarkResult : runResult.getBenchmarkResults()) {
                Iterator<List<IterationResult>> iterator = iterationResults.iterator();
                for (IterationResult iterationResult : benchmarkResult.getIterationResults()) {
                    iterator.next().add(iterationResult);
                }
            }

            for (List<IterationResult> iterationResult : iterationResults) {
                BenchmarkResult benchmarkResult = new BenchmarkResult(iterationResult);

                xData.add(++counter);
                yData.add(benchmarkResult.getPrimaryResult().getScore());
            }
            allData.add(currentSeries);
        }

        sortSeriesBundle(allData,sorting);

        for (SeriesBundle currentSeries : allData) {
            chart.addSeries(currentSeries.getBenchmarkName(), currentSeries.getxData(), currentSeries.getyData());

            Result primaryResult = currentSeries.getRunResult().getAggregatedResult().getPrimaryResult();
            finalChart.addSeries(currentSeries.getBenchmarkName(), new double[]{currentSeries.getxData().get(currentSeries.getxData().size()-1)}, new double[]{primaryResult.getScore()}, new double[]{primaryResult.getScoreError()}, true);
        }

        CompoundChart compoundChart = new CompoundChart(chart, finalChart);
        ChartUtils.exportToDisk(compoundChart, measurementFolder, "chart-all", chart.getSeriesMap().size());

        System.out.println("Exporting graphs");
        for (ChartGroup chartGroup : chartGroups) {
            System.out.println("Exporting " + chartGroup.getTitle());
            saveFilteredChart(chartGroup.getFilter(), chartGroup.getFilename(),chartGroup.getTitle(), measurementFolder, chart, finalChart, null);
        }
        System.out.println("Export done");
    }

    private void writeRawResultTextFile(File measurementFolder, Collection<RunResult> results) {
        try(PrintWriter writer = new PrintWriter(new FileWriter(new File(measurementFolder, "raw.txt")))) {
            ResultFormat instance = ResultFormatFactory.getInstance(ResultFormatType.TEXT, writer);
            instance.writeOut(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFilteredChart(Predicate<String> predicate, String name, String title, File measurementFolder, Chart chart, Chart finalChart, Collection<ChartGroup> renameGroups) {
        Chart filteredChart = createChart(title);
        ChartUtils.filter(chart, filteredChart, renameGroups, predicate, title);
        Chart filteredFinalChart = createFinalChart(title);
        ChartUtils.filter(finalChart, filteredFinalChart, renameGroups, predicate, title);
        if (filteredChart.getSeriesMap().size() < 1) {
            System.out.println("Skip Chart: " + name + ", because it has no Entries");
        } else {
            ChartUtils.exportToDisk(new CompoundChart(filteredChart, filteredFinalChart), measurementFolder, name, filteredChart.getSeriesMap().size());
        }
    }

    public static Chart createChart(String title) {
        final Chart chart = new Chart(500, 500, com.curiousdev.xtchart.StyleManager.ChartTheme.Matlab);
        chart.setChartTitle(title + " Throughput");

        chart.getStyleManager().setLegendVisible(false);
        chart.getStyleManager().setChartPadding(1);
        chart.setXAxisTitle("Time (s)");
        chart.setYAxisTitle("msg/s");

        return chart;
    }

    public static Chart createFinalChart(String title) {
        final Chart chart = new Chart(150, 500, com.curiousdev.xtchart.StyleManager.ChartTheme.Matlab);
        chart.getStyleManager().setLegendPosition(StyleManager.LegendPosition.OutsideE);
        chart.getStyleManager().setLegendVisible(true);
        chart.getStyleManager().setYAxisTicksVisible(false);
        chart.getStyleManager().setFinalValueChart(true);
        chart.getStyleManager().setChartPadding(1);

        return chart;
    }

}
