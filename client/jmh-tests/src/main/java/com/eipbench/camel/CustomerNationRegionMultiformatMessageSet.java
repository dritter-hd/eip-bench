package com.eipbench.camel;

import com.eipbench.generator.MessageSetLoader;

import java.io.File;

public class CustomerNationRegionMultiformatMessageSet extends MessageSetLoader {
    public static final String MESSAGE_FILENAME = "tpch_customer_nation_region-multiformat-150k.json";
    public static final File MESSAGE_SET_FILE = new File(OUTPUT_DIR, MESSAGE_FILENAME);

    public enum NationNames {
        NAME("name"),
        TYPE("type"),
        N_NATIONKEY,
        N_NAME,
        N_REGIONKEY,
        N_COMMENTT;

        private String toStringValue;

        NationNames(String toStringValue) {
            this.toStringValue = toStringValue;
        }
        NationNames() {
            this.toStringValue = null;
        }
        @Override
        public String toString() {
            if (toStringValue != null) {
                return toStringValue;
            } else {
                return super.toString();
            }
        }
    }

    public enum RegionNames {
        NAME("name"),
        TYPE("type"),
        R_REGIONKEY,
        R_NAME,
        R_COMMENT;

        private String toStringValue;

        RegionNames(String toStringValue) {
            this.toStringValue = toStringValue;
        }
        RegionNames() {
            this.toStringValue = null;
        }
        @Override
        public String toString() {
            if (toStringValue != null) {
                return toStringValue;
            } else {
                return super.toString();
            }
        }
    }

    public CustomerNationRegionMultiformatMessageSet() {
        super(MESSAGE_FILENAME);
    }

    private static CustomerNationRegionMultiformatMessageSet INSTANCE;

    public static CustomerNationRegionMultiformatMessageSet read() {
        if (INSTANCE == null) {
            INSTANCE = new CustomerNationRegionMultiformatMessageSet();
            INSTANCE.parse(1, -1);
            INSTANCE.parseDatalog(1,false);
        }
        return INSTANCE;
    }
}
