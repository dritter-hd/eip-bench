package com.eipbench.tpchgenerator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TpchMetaTable {
    private String tableName;
    private Map<String, String> fields = new LinkedHashMap<String, String>(); 
    
    public Map<String, String> getFields() {
        return fields;
    }

    public TpchMetaTable(final String tableName) {
        this.tableName = tableName;
    }
    
    public String addField(final String name) {
        fields.put(name, "xsd:string");
        return name;
    }

    public String addField(final String name, final String type) {
        fields.put(name, type);
        return name;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("table=").append(tableName);
        sb.append("\n\t");
        
        final Iterator<Entry<String, String>> iterator = fields.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, String> next = iterator.next();
            sb.append("field=").append(next.getKey()).append(" ").append("of type=").append(next.getValue());
        }
        sb.append("\n");
        return sb.toString();
    }
}
