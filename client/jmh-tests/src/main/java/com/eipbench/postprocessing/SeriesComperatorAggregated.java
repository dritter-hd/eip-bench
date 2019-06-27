package com.eipbench.postprocessing;

import java.util.Comparator;

public class SeriesComperatorAggregated implements Comparator<SeriesBundle> {
    @Override
    public int compare(SeriesBundle o1, SeriesBundle o2) {
        double result = o2.getAggregatedScoreY() - o1.getAggregatedScoreY();
        if (result < 0) {
            return -1;
        } else {
            if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}