package com.eipbench.postprocessing;

import com.curiousdev.xtchart.BitmapEncoder;
import com.curiousdev.xtchart.Chart;
import com.curiousdev.xtchart.Series;
import com.curiousdev.xtchart.VectorGraphicsEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public class ChartUtils {
    private final static double SCALE_PER_ENTRY = 0.034;
    private final static double SCALE_BASE = 0.9;

    private static double dynamicScaleFactor(int numberOfSeries) {
        return SCALE_BASE + SCALE_PER_ENTRY * numberOfSeries;
    }

    public static void exportToDisk(Chart chart, File folder, String id, int numberOfSeries) {
        try {
            System.out.println("Write PNG");
            String fileName = folder.getAbsolutePath() + "/" + id;
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);

            System.out.println("Write PDF");
            //VectorGraphicsEncoder.saveVectorGraphic(chart, fileName, VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            VectorGraphicsEncoder.saveVectorGraphicLarge(chart, fileName + "-large", VectorGraphicsEncoder.VectorGraphicsFormat.PDF, 2.5);
            VectorGraphicsEncoder.saveVectorGraphicLarge(chart, fileName + "-dynamic", VectorGraphicsEncoder.VectorGraphicsFormat.PDF, dynamicScaleFactor(numberOfSeries));
            VectorGraphicsEncoder.saveVectorGraphicLarge(chart, fileName + "-small", VectorGraphicsEncoder.VectorGraphicsFormat.PDF, 0.7);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void filter(Chart originChart, Chart targetChart, Collection<ChartGroup> renameGroups, Predicate<String> predicate, String title) {
        Map<String, Series> seriesMap = originChart.getSeriesMap();
        seriesMap.keySet().stream()
                .filter(predicate)
                .forEach(series -> targetChart.addSeries(rename(series.replaceAll("_", " "), renameGroups, title), seriesMap.get(series).getXData(), seriesMap.get(series).getYData(), seriesMap.get(series).getErrorBars(), true));
    }

    private static String rename(String benchmarkName, Collection<ChartGroup> renameGroups, String title) {
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
}
