package com.eipbench.postprocessing;

import gnu.trove.list.array.TDoubleArrayList;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;

public class SeriesBundle {
    private String benchmarkName = "";
    private TDoubleArrayList xData;
    private TDoubleArrayList yData;
    private RunResult runResult;

    public SeriesBundle(String benchmarkName, TDoubleArrayList xData, TDoubleArrayList yData, RunResult runResult) {
        super();
        this.xData = xData;
        this.yData = yData;
        this.benchmarkName = benchmarkName;
        this.runResult = runResult;
    }

    public double getLastScoreY() {
        if (yData.isEmpty()) {
            return 0;
        } else {
            return yData.get(yData.size()-1);
        }
    }

    public double getAggregatedScoreY() {
        if (yData.isEmpty()) {
            return 0;
        } else {
            Result primaryResult = runResult.getAggregatedResult().getPrimaryResult();
            return primaryResult.getScore();
        }
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public TDoubleArrayList getxData() {
        return xData;
    }

    public TDoubleArrayList getyData() {
        return yData;
    }

    public RunResult getRunResult() {
        return runResult;
    }
}