package com.eipbench.postprocessing;

import com.curiousdev.xtchart.Chart;
import com.curiousdev.xtchart.StyleManager;
import gnu.trove.list.array.TDoubleArrayList;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormat;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Predicate;

public class ScaleThroughputPostProcessing extends BasicPostProcessing {

    public void process(File folder, Map<Integer, Collection<RunResult>> scaleResults, Collection<ChartGroup> chartGroups,
            SeriesSorting sorting) {
        this.process(folder, scaleResults, chartGroups, null, sorting);
    }

    public void process(File folder, Map<Integer, Collection<RunResult>> scaleResults, Collection<ChartGroup> chartGroups, Collection<ChartGroup> renameGroups, SeriesSorting sorting) {
        final SortedSet<Integer> scales = new TreeSet<>(scaleResults.keySet());

        final TDoubleArrayList xData = new TDoubleArrayList(scaleResults.keySet().size());
        scales.forEach(xData::add);

        final Map<String, TDoubleArrayList> yData = new HashMap<>();
        final Map<String, TDoubleArrayList> errorData = new HashMap<>();

        final ArrayList<SeriesBundle> allData = new ArrayList();

        try(final PrintWriter writer = new PrintWriter(new FileWriter(new File(folder, "raw.txt")))) {
            for (final Integer scaleLevel : scales) {
                writer.println("# Scale level: " + scaleLevel);
                final Collection<RunResult> results = scaleResults.get(scaleLevel);
                final ResultFormat instance = ResultFormatFactory.getInstance(ResultFormatType.TEXT, writer);
                instance.writeOut(results);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        for (final Integer scaleLevel : scales) {
            final Collection<RunResult> results = scaleResults.get(scaleLevel);

            final boolean containsOffHeapOnHeapVariants = checkContainsOffHeapOnHeapVariants(results);
            for (final RunResult runResult : results) {
                final String name = runResult.getParams().getBenchmark();
                String benchmarkName = name.substring(name.lastIndexOf(".",name.lastIndexOf(".")-1) + 1);
                if (containsOffHeapOnHeapVariants) {
                    benchmarkName = benchmarkName + (Boolean.parseBoolean(runResult.getParams().getParam("offHeapMessages")) ? "-offheap" : "");
                }

                TDoubleArrayList yValues = yData.get(benchmarkName);
                if (yValues == null) {
                    yValues = new TDoubleArrayList();
                    yData.put(benchmarkName, yValues);
                }
                yValues.add(runResult.getPrimaryResult().getScore());

                TDoubleArrayList errorValues = errorData.get(benchmarkName);
                if (errorValues == null) {
                    errorValues = new TDoubleArrayList();
                    errorData.put(benchmarkName, errorValues);
                }
                errorValues.add(runResult.getPrimaryResult().getScoreError());
            }
        }

        final Chart chart = createChart("", getParam(scaleResults, "scaleName"), getParam(scaleResults, "scaleUnit"));

        for (final String seriesName : yData.keySet()) {
            final ScaleSeriesBundle currentSeries = new ScaleSeriesBundle(seriesName,xData,yData.get(seriesName), null, errorData.get(seriesName), true);
            allData.add(currentSeries);
        }
        sortSeriesBundle(allData,sorting);

        for (final SeriesBundle cs : allData) {
            final ScaleSeriesBundle currentSeries = (ScaleSeriesBundle) cs;
            chart.addSeries(currentSeries.getBenchmarkName(), currentSeries.getxData(), currentSeries.getyData(), currentSeries.getErrorData(), currentSeries.isOnlyPositiveErrorData());
        }
        ChartUtils.exportToDisk(chart, folder, "chart", chart.getSeriesMap().size());

        System.out.println("Exporting graphs");
        for (final ChartGroup chartGroup : chartGroups) {
            System.out.println("Exporting " + chartGroup.getTitle());
            saveFilteredChart(chartGroup.getFilter(), chartGroup.getFilename(), chartGroup.getTitle(), getParam(scaleResults, "scaleName"), getParam(scaleResults, "scaleUnit"), folder, chart, renameGroups);
        }
        System.out.println("Export done");
    }

    private String rename(String benchmarkName, Collection<ChartGroup> renameGroups) {
        if (renameGroups != null && renameGroups.size() > 0) {
            for (ChartGroup renameGroup : renameGroups) {
                if (renameGroup.getFilter().test(benchmarkName)) {
                    return renameGroup.getTitle();
                }
            }
            return benchmarkName;
        }
        return benchmarkName;
    }

    private void saveFilteredChart(Predicate<String> predicate, String name, String title, String scaleDimension, String scaleUnit,
            File measurementFolder, Chart chart, Collection<ChartGroup> renameGroups) {
        /*Chart filteredChart = createChart(title, scaleDimension, scaleUnit);
        ChartUtils.exportToDisk(chart, measurementFolder, name + "hack", chart.getSeriesMap().size());
        double max = chart.chartPainter.getAxisPair().getChartPainter().getAxisPair().getYAxis().getMax();
        double min = chart.chartPainter.getAxisPair().getChartPainter().getAxisPair().getYAxis().getMin();
        System.out.println(max);
        ChartUtils.filter(chart, filteredChart, renameGroups, predicate, title);
        filteredChart.chartPainter.getAxisPair().getYAxis().setMax(max);
        filteredChart.chartPainter.getAxisPair().getYAxis().setMin(min);
        ChartUtils.exportToDisk(filteredChart, measurementFolder, name, chart.getSeriesMap().size());*/
    }

    private String getParam(Map<Integer, Collection<RunResult>> scaleResults, String key) {
        // TODO: Avoid this train wreck
        return scaleResults.entrySet().iterator().next().getValue().iterator().next().getParams().getParam(key);
    }

    public static Chart createChart(String title, String scaleDimension, String scaleUnit) {
        final Chart chart = new Chart(600, 500, com.curiousdev.xtchart.StyleManager.ChartTheme.Matlab);
        chart.setChartTitle(title + " - " + scaleDimension + " Scale");
        chart.getStyleManager().setChartTitleVisible(false);

        chart.getStyleManager().setLegendVisible(true);
        chart.getStyleManager().setLegendPosition(StyleManager.LegendPosition.InsideNE);
        chart.getStyleManager().setChartPadding(1);
        chart.getStyleManager().setPlotGridLinesVisible(false);
        chart.setXAxisTitle(scaleDimension + " " + scaleUnit);
        chart.setYAxisTitle("msg/s");
        chart.getStyleManager().setXAxisLogarithmic(true);
        chart.getStyleManager().setYAxisLogarithmic(true);

        return chart;
    }
}
