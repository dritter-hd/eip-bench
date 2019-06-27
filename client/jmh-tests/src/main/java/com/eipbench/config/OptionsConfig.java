package com.eipbench.config;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsConfig {
    private int warmupIterations = 10;
    private int measurementIterations = 20;
    private int forks = 5;
    private boolean run = true;
    private List<String> exclude = new ArrayList<>();
    private List<String> include = new ArrayList<>();

    public OptionsConfig() {
        super();
        include.add(".*");
    }

    public void loadJsonConfig(final JSONObject node) throws JSONException {
        if (node.has("warmupIterations")) {
            this.warmupIterations = node.getInt("warmupIterations");
        }
        if (node.has("measurementIterations")) {
            this.measurementIterations = node.getInt("measurementIterations");
        }
        if (node.has("forks")) {
            this.forks = node.getInt("forks");
        }
        if (node.has("run")) {
            this.run = node.getBoolean("run");
        }
        if (node.has("exclude")) {
            JSONArray excludeNode = node.getJSONArray("exclude");
            exclude.clear();
            for (int i = 0; i < excludeNode.length(); i++) {
                exclude.add(excludeNode.getString(i));
            }
        }
        if (node.has("include")) {
            JSONArray includeNode = node.getJSONArray("include");
            include.clear();
            for (int i = 0; i < includeNode.length(); i++) {
                include.add(includeNode.getString(i));
            }
        }
    }

    public void printConfig(final String prefix) {
        System.out.println(prefix + "warmupIterations" + ": " + warmupIterations);
        System.out.println(prefix + "measurementIterations" + ": " + measurementIterations);
        System.out.println(prefix + "forks" + ": " + forks);
        System.out.println(prefix + "run" + ": " + run);
        System.out.println(prefix + "exclude" + ": " + Arrays.toString(exclude.toArray()));
        System.out.println(prefix + "include" + ": " + Arrays.toString(include.toArray()));
    }

    public ChainedOptionsBuilder apply(final ChainedOptionsBuilder builder) {
        builder.warmupIterations(warmupIterations).measurementIterations(measurementIterations).forks(forks);
        for (String s : exclude) {
            builder.exclude(s);
        }
        for (String s : include) {
            builder.include(s);
        }
        return builder;
    }

    public int getWarmupIterations() {
        return warmupIterations;
    }

    public void setWarmupIterations(int warmupIterations) {
        this.warmupIterations = warmupIterations;
    }

    public int getMeasurementIterations() {
        return measurementIterations;
    }

    public void setMeasurementIterations(int measurementIterations) {
        this.measurementIterations = measurementIterations;
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(final List<String> exclude) {
        this.exclude = exclude;
    }

    public List<String> getInclude() {
        return include;
    }

    public void setInclude(final List<String> include) {
        this.include = include;
    }
}
