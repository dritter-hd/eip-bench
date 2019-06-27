package com.eipbench.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DatalogFact {
    private List<Object> parameters = new ArrayList<Object>();
    private static final transient Logger LOG = LoggerFactory.getLogger(DatalogFact.class);

    public void insertParamater(Object parameterValue) {
        parameters.add(parameterValue);
    }

    public String generateDatalogFactBody() throws UnsupportedDataTypeException {
        StringBuilder fact = new StringBuilder();
        for (Object parameter : parameters) {
            fact.append(getAttributeValue(parameter) + ",");
        }
        return fact.substring(0, fact.lastIndexOf(","));
    }

    public List<Object> getParameters() {
        return parameters;
    }

    protected String getAttributeValue(Object object) throws UnsupportedDataTypeException {
        if (object == null) {
            return "\"null\"";
        }

        if (object instanceof Integer) {
            return object.toString();
        }

        if (object instanceof Boolean) {
            return Boolean.toString((Boolean) object);
        }

        /*
         * No long support in Hlog if (object instanceof Long) { return
         * object.toString(); }
         */

        if (object instanceof Double) {
            return object.toString();
        }

        if (object instanceof String) {
            // TODO: parse Dates in ISO 8601 Format (there was some bug when
            // trying to parse with DateFormat!)
            String string = (String) object;

            // #####hack to get numbers coded as string as integer/double as the
            // xmlToJsonMarshaller encodes numbers as String
            try {
                Integer integerRepresentationOfString = Integer.parseInt(string);
                return integerRepresentationOfString.toString();
            } catch (NumberFormatException e) {
            }

            try {
                Double doubleRepresentationOfString = Double.parseDouble(string);
                return doubleRepresentationOfString.toString();
            } catch (NumberFormatException e) {

            }

            String cleanedString = ConversionHelper.cleanString(string);

            if (cleanedString.length() != string.length()) {
                LOG.warn("Replaced several unsupported characters");
            }

            return "\"" + cleanedString + "\"";
        }

        LOG.warn("Unsupported Datatype: " + object.getClass().getSimpleName());
        return "\"" + object.toString() + "\"";
    }
    
    public boolean isEmpty(){
        return parameters.isEmpty();
    }
}
