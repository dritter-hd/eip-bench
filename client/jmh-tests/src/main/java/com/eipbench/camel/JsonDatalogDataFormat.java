package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.TypeConversionException;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.rmi.MarshalException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JsonDatalogDataFormat implements DataFormat {
    private static final Logger log = LoggerFactory.getLogger(JsonDatalogDataFormat.class);
    private boolean generateRandomIds = true;
    private boolean appendMissingVariables = true;
    private DatalogMetaFacts metaFacts = null;
    String filename;
    private static final transient Logger LOG = LoggerFactory.getLogger(JsonDatalogDataFormat.class);
    private String endpointUri;

    private boolean skipMetaFactGeneration = false;

    private final ObjectMapper om = new ObjectMapper();

    public JsonDatalogDataFormat() {
    }

    public JsonDatalogDataFormat(DatalogMetaFacts metaFacts) {
        this.metaFacts = metaFacts;
    }

    public JsonDatalogDataFormat(boolean generateRandomIds) {
        this.generateRandomIds = generateRandomIds;
    }

    public JsonDatalogDataFormat(boolean generateRandomIds, boolean appendMissingVariables) {
        this.generateRandomIds = generateRandomIds;
        this.appendMissingVariables = appendMissingVariables;
    }

    public JsonDatalogDataFormat(DatalogMetaFacts metaFacts, boolean appendMissingVariables) {
        this.appendMissingVariables = appendMissingVariables;
        this.metaFacts = metaFacts;
    }

    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        throw new NotYetImplementedException();
    }

    @Deprecated
    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        byte[] byteArray = IOUtils.toByteArray(stream);
        InputStream input1 = new ByteArrayInputStream(byteArray);
        InputStream input2 = new ByteArrayInputStream(byteArray);

        Object body;
        try {
            ObjectInput ois = new ObjectInputStream(input1);
            body = ois.readObject();
        } catch (Exception e) {
            try {
                body = IOUtils.toString(input2, Charset.forName("UTF-8"));
            } catch (Exception e2) {
                throw new MarshalException("Could not convert inputStream");
            }
        }
        return unmarshal(exchange, body);
    }

    public Object unmarshal(final Exchange exchange, Object body) throws UnsupportedDataTypeException {
        final String message = exchange.getIn().getBody(String.class);

        if (!isSkipMetaFactGeneration()) {
            setFileNameToConsumerURI(exchange);
        }

        return unmarshal(body, message);
    }

    public Object unmarshal(Object body, String message) throws UnsupportedDataTypeException {
        JsonDatalog jsonDatalog = null;

        if (body instanceof JsonNode) {
            if (((JsonNode) body).isObject()) {
                jsonDatalog = new JsonDatalogObject((JsonNode) body, "root", null, generateRandomIds, appendMissingVariables);
            }
            if (((JsonNode) body).isArray()) {
                jsonDatalog = new JsonDatalogArray((JsonNode) body, "root", null, generateRandomIds, appendMissingVariables);
            }
        } else {
            try {
                String bodyAsString = message;
                jsonDatalog = parseJsonDatalogString(bodyAsString, appendMissingVariables);
            } catch (TypeConversionException e) {
                throw new RuntimeCamelException("Message is neither of type JSONObject nor of type JSONArray");
            }
        }

        DatalogProgramCreator creator = new DatalogProgramCreator();

        if (!skipMetaFactGeneration) {
            if (metaFacts == null) {
                this.metaFacts = loadMetaFactsFromFile();
            }
            creator.setMetaFacts(this.metaFacts);
        }

        extractDataLogFacts(jsonDatalog, creator);
        DatalogProgram program = creator.generateDatalogProgram();

        if (!skipMetaFactGeneration) {
            saveMetaFactsToFile(creator.getMetaFacts());
        }
        return program;
    }

    private void setFileNameToConsumerURI(Exchange exchange) {
        Consumer consumer = exchange.getContext().getRoutes().get(0).getConsumer();
        endpointUri = consumer.getEndpoint().getEndpointUri();

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(endpointUri.getBytes());
            byte byteData[] = messageDigest.digest();
            String hash = convertToHexString(byteData);
            this.filename = hash + ".camelhlog";
        } catch (NoSuchAlgorithmException e) {
            this.filename = endpointUri.replaceAll("[^a-zA-Z0-9 -]", "") + ".camelhlog";
        }
    }

    private String convertToHexString(byte[] byteData) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private void saveMetaFactsToFile(DatalogMetaFacts metaFacts) {
        String stringToSave = metaFacts.getStringRepresentationForSave();
        File metaFactConfigFile = new File(filename);
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(metaFactConfigFile);
            printWriter.print(endpointUri);
            printWriter.println("----------------");
            printWriter.print(stringToSave);
            printWriter.flush();
        } catch (FileNotFoundException e) {
            LOG.error("Exception occured while trying to save the metaFactsConfiFile", e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }

    }

    private DatalogMetaFacts loadMetaFactsFromFile() {
        File metaFactConfigFile = new File(filename);
        if (!metaFactConfigFile.exists()) {
            return new DatalogMetaFacts();
        }
        FileReader fileReader = null;
        BufferedReader reader = null;
        try {
            fileReader = new FileReader(metaFactConfigFile);
            reader = new BufferedReader(fileReader);
            DatalogMetaFacts metaFacts = new DatalogMetaFacts();
            boolean endOfHeaderReached = false;
            while (reader.ready()) {
                if (!endOfHeaderReached) {
                    if (reader.readLine().contains("------")) {
                        endOfHeaderReached = true;
                    }
                    continue;
                }
                String metaFactString = reader.readLine();
                String[] predicateAndParameterNames = metaFactString.split("=");
                String predicateName = predicateAndParameterNames[0];
                String parameterNames = predicateAndParameterNames[1];
                DatalogMetaFact metaFact = createMetaFactFrom(parameterNames);
                metaFacts.add(predicateName, metaFact);
            }
            return metaFacts;
        } catch (FileNotFoundException e) {
            LOG.error("Exception occured reading from metaFactsFile", e);
        } catch (IOException e) {
            LOG.error("Exception occured reading from metaFactsFile", e);
        } finally {
            try {
                reader.close();
                fileReader.close();
            } catch (IOException e) {
                LOG.error("Exception occured while freeing resources from metaFactsFile read", e);
            }

        }
        return new DatalogMetaFacts();
    }

    private JsonDatalog parseJsonDatalogString(String tweets, boolean appendMissingVariables) {
        log.debug("Attempting to parse message: " + tweets);

        try {
            final JsonNode jsonNode = om.readTree(tweets);

            if (jsonNode.isArray()) {
                return new JsonDatalogArray(jsonNode, "root", null, generateRandomIds, appendMissingVariables);
            }
            if (jsonNode.isObject()) {
                return new JsonDatalogObject(jsonNode, "root", null, generateRandomIds, appendMissingVariables);
            }

            throw new RuntimeException("Could not parse json datalog string because no array or object JSON found.");
        } catch (IOException e) {
            throw new RuntimeException("Could not parse json datalog string: " + e.getMessage(), e);
        }
    }

    protected void extractDataLogFacts(final JsonDatalog jsonDatalog, final DatalogProgramCreator creator) throws UnsupportedDataTypeException {
        jsonDatalog.parseJson(creator);
        // LOG.debug("Nested Object Size: "+jsonDatalog.getNestedObjects().size());
        for (JsonDatalog json : jsonDatalog.getNestedObjects()) {
            extractDataLogFacts(json, creator);
            LOG.debug("Processing nested Object");
        }
    }

    private DatalogMetaFact createMetaFactFrom(String parameterNames) {
        String[] parameterNameArray = parameterNames.split(",");
        DatalogMetaFact metaFact = new DatalogMetaFact();
        for (String parameterName : parameterNameArray) {
            metaFact.addParameter(parameterName);
        }
        return metaFact;
    }

    public boolean isSkipMetaFactGeneration() {
        return skipMetaFactGeneration;
    }

    public void setSkipMetaFactGeneration(boolean skipMetaFactGeneration) {
        this.skipMetaFactGeneration = skipMetaFactGeneration;
    }
}
