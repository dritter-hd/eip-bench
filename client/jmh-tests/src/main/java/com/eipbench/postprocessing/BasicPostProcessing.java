package com.eipbench.postprocessing;

import org.openjdk.jmh.results.RunResult;

import java.util.ArrayList;
import java.util.Collection;

public class BasicPostProcessing {
    protected boolean checkContainsOffHeapOnHeapVariants(Collection<RunResult> results) {
        boolean containsOnHeap = false;
        boolean containsOffHeap = false;

        for (RunResult result : results) {
            if (Boolean.parseBoolean(result.getParams().getParam("offHeapMessages"))) {
                containsOffHeap = true;
            } else {
                containsOnHeap = true;
            }

            if (containsOnHeap && containsOffHeap) {
                return true;
            }
        }

        return false;
    }

    protected void sortSeriesBundle(ArrayList<SeriesBundle> list, SeriesSorting sorting) {
        switch (sorting) {
        case ALPHABETICAL:break;
        case LASTENTRY_VALUE:list.sort(new SeriesComperatorLast());
                             break;
        case AGGREGATED_VALUE:list.sort(new SeriesComperatorAggregated());
                              break;
        }
    }
}
