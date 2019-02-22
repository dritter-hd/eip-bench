package com.eipbench.pdgf;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;
import pdgf.Controller;
import pdgf.core.exceptions.XmlException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class TpchDataGenerator {
    private static final File BASEDIR = new File(System.getProperty("java.io.tmpdir") + File.separator + "eipbench");
    public static final File TPCH_SCHEMA_FILE = new File(BASEDIR, "tpchSchema.xml");
    public static final File OUTPUT_DIR = new File(BASEDIR, "output");
    private static final File PSEUDO_TEXT_GEN_PROP_FILE = new File(BASEDIR, "config" + File.separator + "PseudoTextGenerator.properties");

    public void prepareWorkingDir() {
        BASEDIR.mkdirs();
        File dictsDir = new File(BASEDIR, "dicts");
        dictsDir.mkdir();
        FileUtils.copyResourcesRecursively(getClass().getResource("/dicts"), dictsDir);

        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "class");
        engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityContext context = new VelocityContext();
        new File(BASEDIR, "output").mkdirs();
        context.put("benchout", "output");
        context.put("benchbase", BASEDIR.getAbsolutePath());
        context.put("benchdicts", "dicts");
        context.put("scaleFactor", 1);
        context.put("separator", File.separator);
        context.put("esc", new EscapeTool());

        writeTpchSchemaFile(engine, context);
        writePseudoTextGenPropertiesFile(engine, context);
    }

    private void writePseudoTextGenPropertiesFile(VelocityEngine engine, VelocityContext context) {
        Template template = engine.getTemplate("config" + File.separator + "PseudoTextGenerator.properties.vm");
        writeTemplate(template, context, PSEUDO_TEXT_GEN_PROP_FILE);

        try {
            ClassPathHacker.addFile(BASEDIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PseudoTextGenerator.properties into classpath", e);
        }
    }

    private void writeTpchSchemaFile(VelocityEngine engine, VelocityContext context) {
        Template template = engine.getTemplate("config" + File.separator + "tpchSchema.xml.vm");
        writeTemplate(template, context, TPCH_SCHEMA_FILE);
    }

    private void writeTemplate(Template template, VelocityContext context, File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            template.merge(context, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write template for file: " + file.getAbsolutePath(), e);
        }
    }

    public void generate() {
        Controller instance = null;
        try {
            instance = new Controller();
        } catch (XmlException e) {
            e.printStackTrace();
        }

        String[] args = { "load", TPCH_SCHEMA_FILE.getAbsolutePath() };
        try {
            // instance.executeCommand(new String[]{"noShell"});
            instance.executeCommand(args);

            instance.executeCommand(new String[] { "start" });

            instance.executeCommand(new String[] { "closewhendone", "true" });
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Yet another ugly hack to wait for generated files */
        final Object lock = new Object();
        synchronized (lock) {
            instance.getDataGenerator().addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    if (((String) arg).startsWith("finished processing after")) {
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }
            });

            try {
                lock.wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public File getWorkingDir() {
        return BASEDIR;
    }

    public File getOutputDir() {
        return OUTPUT_DIR;
    }
}
