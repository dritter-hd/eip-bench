package com.eipbench.camel;

import com.eipbench.generator.MessageSetLoader;

import java.io.File;

public class CustomerNationRegionEmbeddedMessageSet extends MessageSetLoader {
    public static final String MESSAGE_FILENAME = "tpch_customer-embedding-150-k.json";
    public static final File MESSAGE_SET_FILE = new File(OUTPUT_DIR, MESSAGE_FILENAME);

    public enum CustomerNames {
        NAME("name"),
        TYPE("type"),
        C_CUSTKEY,
        C_NAME,
        C_ADRESS,
        C_NATIONKEY,
        C_PHONE,
        C_ACCTBAL,
        C_MKTSEGMENT,
        C_COMMENT;

        private String toStringValue;

        CustomerNames(String toStringValue) {
            this.toStringValue = toStringValue;
        }
        CustomerNames() {
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

    public CustomerNationRegionEmbeddedMessageSet() {
        super(MESSAGE_FILENAME);
    }

    private static CustomerNationRegionEmbeddedMessageSet INSTANCE;

    public static CustomerNationRegionEmbeddedMessageSet read() {
        if (INSTANCE == null) {
            INSTANCE = new CustomerNationRegionEmbeddedMessageSet();
            INSTANCE.parse(1, -1);
            INSTANCE.parseDatalog(1,false);
        }
        return INSTANCE;
    }
}
