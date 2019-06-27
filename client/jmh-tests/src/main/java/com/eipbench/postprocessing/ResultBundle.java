package com.eipbench.postprocessing;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class ResultBundle {
    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));
    private static final File EIPBENCH_DIR = new File(TMP_DIR, "eipbench");
    public static final File RESULT_DIR = new File(EIPBENCH_DIR, "results");

    private final File bundleBase;

    public ResultBundle(final String prefix) {
        RESULT_DIR.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        bundleBase = new File(RESULT_DIR, prefix + timeStamp);
        bundleBase.mkdirs();

        writeGitInformation();
        writeHostInformation();
    }

    private void writeHostInformation() {
        Properties properties = System.getProperties();
        properties.setProperty("os.cores", String.valueOf(Runtime.getRuntime().availableProcessors()));
        try {
            properties.store(new FileOutputStream(new File(bundleBase, "system.properties")), "bench");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeGitInformation() {
        InputStream gitProperties = this.getClass().getResourceAsStream("/git.properties");
        if (gitProperties != null) {
            System.out.println("Writing git properties");
            try {
                IOUtils.copy(gitProperties, new FileOutputStream(new File(bundleBase, "git.properties")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File createMeasurement(String id) {
        File file = new File(bundleBase, id);
        file.mkdirs();
        return file;
    }
}
