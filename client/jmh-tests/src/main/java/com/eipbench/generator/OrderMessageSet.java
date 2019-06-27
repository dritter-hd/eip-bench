package com.eipbench.generator;

import java.io.File;

public class OrderMessageSet extends MessageSetLoader{
    public static final String MESSAGE_FILENAME = "tpch_order-1_5-mio.json";
    public static final File MESSAGE_SET_FILE = new File(OUTPUT_DIR, MESSAGE_FILENAME);

    public enum OrderNames {
        NAME("name"),
        TYPE("type"),
        O_ORDERKEY,
        O_CUSTKEY,
        O_ORDERSTATUS,
        O_TOTALPRICE,
        O_ORDERDATE,
        O_ORDERPRIORITY,
        O_CLERK,
        O_SHIPPRIORITY,
        O_COMMENT;

        private String toStringValue;

        OrderNames(String toStringValue) {
            this.toStringValue = toStringValue;
        }
        OrderNames() {
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

    public OrderMessageSet() {
        super(MESSAGE_FILENAME);
    }

    private static OrderMessageSet INSTANCE;

    public static OrderMessageSet read() {
        if (INSTANCE == null) {
            INSTANCE = new OrderMessageSet();
            INSTANCE.parse(1, -1);
            INSTANCE.parseDatalog(1,false);
        }
        return INSTANCE;
    }
}
