package com.eipbench.postprocessing;

import java.util.function.Predicate;

public class ChartGroup {
    private final Predicate<String> filter;
    private final String filename;
    private final String title;

    public ChartGroup(Predicate<String> filter, String filename, String title) {
        this.filter = filter;
        this.filename = filename;
        this.title = title;
    }

    public Predicate<String> getFilter() {
        return filter;
    }

    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }
}
