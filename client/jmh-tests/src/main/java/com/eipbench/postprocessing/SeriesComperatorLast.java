package com.eipbench.postprocessing;

import java.util.Comparator;

public class SeriesComperatorLast implements Comparator<SeriesBundle> {
    @Override
    public int compare(SeriesBundle o1, SeriesBundle o2) {
        double result = o2.getLastScoreY() - o1.getLastScoreY();
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