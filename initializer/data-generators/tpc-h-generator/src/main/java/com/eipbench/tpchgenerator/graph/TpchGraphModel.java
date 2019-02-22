package com.eipbench.tpchgenerator.graph;

import com.eipbench.tpchgenerator.TpchMetaTable;
import com.eipbench.tpchgenerator.TpchTableSchemaParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class TpchGraphModel {
    private static final String SCHEMA_TPCH_SCHEMA_XML = "/schema/tpchSchema.xml";

    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";

    private Map<String, TpchMetaTable> tableMetaData;

    private Map<String, String> typeMapping;

    public TpchGraphModel() {
        final TpchTableSchemaParser tpchTableSchemaParser = new TpchTableSchemaParser();
        try {
            tableMetaData = tpchTableSchemaParser.parse(this.getClass().getResourceAsStream(SCHEMA_TPCH_SCHEMA_XML));
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Tpch table schema parser exception", e);
        }

        typeMapping = new HashMap<String, String>();
        typeMapping.put("java.sql.Types.INTEGER", "Int");
        typeMapping.put("java.sql.Types.DECIMAL", "Double");
        typeMapping.put("java.sql.Types.VARCHAR", "String");
        typeMapping.put("java.sql.Types.DATE", "Date");
    }

    public static enum TABLE_NAMES {
        NATION, REGION, PART, SUPPLIER, PARTSUPP, CUSTOMER, ORDERS, LINEITEM
    }

    public static enum DIRECTION {
        forward, backward
    }
    
    public static enum TYPE {
        singleformat, multiformat, embedding
    }

    public static enum EDGE_TYPES {
        NATION_TO_REGION(TABLE_NAMES.NATION, TABLE_NAMES.REGION, "N_REGIONKEY", "R_REGIONKEY"), SUPPLIER_TO_NATION(TABLE_NAMES.SUPPLIER,
                TABLE_NAMES.NATION, "S_NATIONKEY", "N_NATIONKEY"), CUSTOMER_TO_NATION(TABLE_NAMES.CUSTOMER, TABLE_NAMES.NATION,
                "C_NATIONKEY", "N_NATIONKEY"), PARTSUPP_TO_SUPPLIER(TABLE_NAMES.PARTSUPP, TABLE_NAMES.SUPPLIER, "PS_SUPPKEY", "S_SUPPKEY"), PARTSUPP_TO_PART(
                TABLE_NAMES.PARTSUPP, TABLE_NAMES.PART, "PS_PARTKEY", "P_PARTKEY"), LINEITEM_TO_PARTSUPP(TABLE_NAMES.LINEITEM,
                TABLE_NAMES.PARTSUPP, "X", "X"), ORDERS_TO_CUSTOMER(TABLE_NAMES.ORDERS, TABLE_NAMES.CUSTOMER, "O_CUSTKEY", "C_CUSTKEY"), LINEITEM_TO_ORDER(
                TABLE_NAMES.LINEITEM, TABLE_NAMES.ORDERS, "L_ORDERKEY", "O_ORDERKEY");

        private final TABLE_NAMES from;
        private final TABLE_NAMES to;
        private final String fromJoinColumn;
        private final String toJoinColumn;

        EDGE_TYPES(TABLE_NAMES from, TABLE_NAMES to, String fromJoinColumn, String toJoinColumn) {
            this.from = from;
            this.to = to;
            this.fromJoinColumn = fromJoinColumn;
            this.toJoinColumn = toJoinColumn;
        }

        public String getFromJoinColumn(final DIRECTION direction) {
            if (direction == DIRECTION.forward) {
                return toJoinColumn;
            } else {
                return fromJoinColumn;
            }
        }

        public String getToJoinColumn(final DIRECTION direction) {
            if (direction == DIRECTION.forward) {
                return fromJoinColumn;
            } else {
                return toJoinColumn;
            }
        }

        public TABLE_NAMES from(final DIRECTION direction) {
            if (direction == DIRECTION.forward) {
                return to;
            } else {
                return from;
            }
        }

        public TABLE_NAMES to(final DIRECTION direction) {
            if (direction == DIRECTION.forward) {
                return from;
            } else {
                return to;
            }
        }

        public static List<TABLE_NAMES> entering(TABLE_NAMES name) {
            final ArrayList<TABLE_NAMES> result = new ArrayList<>();

            for (EDGE_TYPES edge_types : values()) {
                if (edge_types.to == name) {
                    result.add(edge_types.from);
                }
            }
            return result;
        }

        public static List<EDGE_TYPES> edgesForDirection(final TABLE_NAMES name, final DIRECTION direction) {
            final ArrayList<EDGE_TYPES> result = new ArrayList<>();

            if (direction == DIRECTION.forward) {
                for (EDGE_TYPES edge_types : values()) {
                    if (edge_types.from == name) {
                        result.add(edge_types);
                    }
                }
            } else {
                for (EDGE_TYPES edge_types : values()) {
                    if (edge_types.to == name) {
                        result.add(edge_types);
                    }
                }
            }
            return result;
        }
    }

    public Map<String, TpchMetaTable> getTableMetaData() {
        return tableMetaData;
    }

    public Map<String, String> getTypeMapping() {
        return typeMapping;
    }

    public Properties getTableProperties(final TpchMetaTable tpchMetaTable) {
        final Properties props = new Properties();
        props.put("suppressHeaders", "true");

        final StringBuilder headerLine = new StringBuilder();
        final StringBuilder columnTypes = new StringBuilder();

        final Iterator<Entry<String, String>> iterator = tpchMetaTable.getFields().entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, String> next = iterator.next();
            headerLine.append(next.getKey());

            final String mappedType = this.getTypeMapping().get(next.getValue());
            if (null == mappedType) {
                throw new IllegalArgumentException("Missing type mapping for " + next.getValue());
            }
            columnTypes.append(mappedType);

            if (iterator.hasNext()) {
                headerLine.append(",");
                columnTypes.append(",");
            }
        }
        props.put("headerline", headerLine.toString());
        props.put("columnTypes", columnTypes.toString());
        return props;
    }

    public Map<String, List<String>> getJoinKeyMap() {
        final HashMap<String, List<String>> joinKeyMap = new HashMap<>();
        joinKeyMap.put(EDGE_TYPES.NATION_TO_REGION.name(), Arrays.asList("N_REGIONKEY", "R_REGIONKEY"));
        joinKeyMap.put(EDGE_TYPES.SUPPLIER_TO_NATION.name(), Arrays.asList("S_NATIONKEY", "N_NATIONKEY"));
        joinKeyMap.put(EDGE_TYPES.CUSTOMER_TO_NATION.name(), Arrays.asList("C_NATIONKEY", "N_NATIONKEY"));
        joinKeyMap.put(EDGE_TYPES.LINEITEM_TO_ORDER.name(), Arrays.asList("L_ORDERKEY", "O_ORDERKEY"));
        joinKeyMap.put(EDGE_TYPES.ORDERS_TO_CUSTOMER.name(), Arrays.asList("O_CUSTKEY", "C_CUSTKEY"));
        joinKeyMap.put(EDGE_TYPES.PARTSUPP_TO_PART.name(), Arrays.asList("PS_PARTKEY", "P_PARTKEY"));
        joinKeyMap.put(EDGE_TYPES.PARTSUPP_TO_SUPPLIER.name(), Arrays.asList("PS_SUPPKEY", "S_SUPPKEY"));
        return joinKeyMap;
    }
}
