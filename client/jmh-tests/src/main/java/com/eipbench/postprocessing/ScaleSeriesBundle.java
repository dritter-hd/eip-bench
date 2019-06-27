package com.eipbench.postprocessing;

import gnu.trove.list.array.TDoubleArrayList;
import org.openjdk.jmh.results.RunResult;

public class ScaleSeriesBundle extends SeriesBundle {
    private TDoubleArrayList errorData;
    private boolean onlyPositiveErrorData;

    public ScaleSeriesBundle(String benchmarkName, TDoubleArrayList xData, TDoubleArrayList yData, RunResult runResult,
            TDoubleArrayList errorData, boolean onlyPositiveErrorData) {
        super(benchmarkName, xData, yData, runResult);
        this.errorData = errorData;
        this.onlyPositiveErrorData = onlyPositiveErrorData;
    }

    public TDoubleArrayList getErrorData() {
        return errorData;
    }

    public boolean isOnlyPositiveErrorData() {
        return onlyPositiveErrorData;
    }
}
