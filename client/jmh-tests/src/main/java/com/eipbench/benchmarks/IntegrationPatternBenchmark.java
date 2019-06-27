package com.eipbench.benchmarks;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class IntegrationPatternBenchmark {

    public String getCamelImplementation() {
        String classname = this.getClass().getSimpleName();
        return classname.substring(0, 2);
    }

    public String getBenchmarkType() {
        String classname = this.getClass().getSimpleName();
        String camelImplementation = getCamelImplementation();
        return classname.substring(camelImplementation.length());
    }

    public Predicate<String> getPattern() {
        String regex = this.getClass().getSimpleName() + "\\.(.*)$";
        return Pattern.compile(regex).asPredicate();
    }

    public String getFileName() {
        return "chart-" + getBenchmarkType().toLowerCase();
    }
    public abstract String getDisplayName();

}
