package com.eipbench.content;

import com.eipbench.camel.JsonDatalogDataFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.dritter.hd.dlog.IFacts;
import com.github.dritter.hd.dlog.Facts;
import com.github.dritter.hd.dlog.Predicate;
import com.github.dritter.hd.dlog.algebra.DataIterator;
import com.github.dritter.hd.dlog.algebra.FillableTableIterator;
import com.github.dritter.hd.dlog.algebra.ParameterValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageSetLoader {
    protected static final File BASEDIR = new File(System.getProperty("java.io.tmpdir") + File.separator + "eipbench");
    public static final File OUTPUT_DIR = new File(BASEDIR, "output");
    public static final File CACHE_DIR = new File(BASEDIR, "cache");
    protected static final File JAVA_CONTENT_CACHE = new File(OUTPUT_DIR, "java-content.data");
    protected static final File DATALOG_CONTENT_CACHE = new File(OUTPUT_DIR, "datalog-content.data");
    public final File MESSAGE_SET_FILE;

    private CopyOnWriteArrayList<Collection<JsonNode>> messagesList;
    // private Iterator<Collection<JsonNode>> messageListIterator;

    private CopyOnWriteArrayList<Collection<com.github.dritter.hd.dlog.IFacts>> datalogMessagesList;
    // private Iterator<Collection<com.github.dritter.hd.dlog.IFacts>>
    // datalogMessageListIterator;

    private static String regex = "^((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";
    private static Pattern compiledRegex = Pattern.compile(regex);
    private static ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("YYYY-MM-dd");
        }
    };

    private ArrayList<JsonNode> flatMessages;

    public MessageSetLoader(String filename) {
        MESSAGE_SET_FILE = new File(OUTPUT_DIR, filename);
    }

    public Iterator<Collection<JsonNode>> getJsonMessageListIterator() {
        return messagesList.iterator();
    }

    // private static class ParseValues {
    // private static final int START_OBJECT = 1;
    // private static final int END_OBJECT = 2;
    // //private static final int FIELD_NAME = 5;
    // private static final int VALUE_STRING = 6;
    // private static final int VALUE_INT = 7;
    // private static final int VALUE_NUMBER_FLOAT = 8;
    // }

    private ArrayList<JsonNode> parseJavaFile(final File file, final ObjectReader om) throws JsonProcessingException, IOException {
        System.out.println("Parsing: " + file.getAbsolutePath());
        long before = System.currentTimeMillis();
        ArrayList<JsonNode> messages = parseAndTransformJsonToMessage(file, om);
        long after = System.currentTimeMillis();
        System.out.println("Parsing of " + file.getAbsolutePath() + " took " + (after - before) + "ms" + " (Java)");
        return messages;
    }

    // private Collection<IFacts> parseDatalogFile(final File file, final
    // JsonFactory jsonFactory, final int arity) throws JsonProcessingException,
    // IOException {
    // final List<IFacts> fs = new ArrayList<com.github.dritter.hd.dlog.IFacts>();
    // System.out.println("Parsing: " + file.getAbsolutePath());
    // long before = System.currentTimeMillis();
    // Collection<IFacts> messages =
    // parseAndTransformJsonToDatalogMessage(jsonFactory, fs , arity);
    // long after = System.currentTimeMillis();
    // System.out.println("Parsing of " + file.getAbsolutePath() + " took " +
    // (after - before) + "ms" + " (Datalog)");
    // return messages;
    // }

    public void parse(int batchSize, int messageSizeScaleLevel) {
        final ObjectReader om = new ObjectMapper().reader();
        // final JsonFactory jsonFactory = new JsonFactory();
        try {
            JsonDatalogDataFormat jsonDatalogDataFormat = new JsonDatalogDataFormat();
            jsonDatalogDataFormat.setSkipMetaFactGeneration(true);
            // program = (DatalogProgram)S
            // jsonDatalogDataFormat.unmarshal(om.readTree(message), message);

            if (MESSAGE_SET_FILE.exists()) {
                ArrayList<JsonNode> messages;
                if (messageSizeScaleLevel >= 0) { // TODO: is >= 0 correct??
                    messages = parseJavaFile(new File(OUTPUT_DIR, "tpch_order-customer-sizes-sl-" + messageSizeScaleLevel + ".json"), om);
                } else {
                    messages = parseJavaFile(MESSAGE_SET_FILE, om);
                }

                int batchCounter = 0;
                ArrayList<JsonNode> currentBatch = new ArrayList<>(batchSize);
                ArrayList<ArrayList<JsonNode>> allBatches = new ArrayList<>((messages.size() / batchSize) + 1);
                for (JsonNode message : messages) {
                    batchCounter++;
                    currentBatch.add(message);
                    if (batchCounter >= batchSize) {
                        allBatches.add(currentBatch);
                        currentBatch = new ArrayList<>();
                        batchCounter = 0;
                    }
                }
                messagesList = new CopyOnWriteArrayList<>();
                messagesList.addAll(allBatches);

                flatMessages = messages;

                // messageListIterator = messagesList.iterator();
            } else {
                throw new RuntimeException(MESSAGE_SET_FILE.getAbsolutePath() + " does not exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseDatalog(int batchSize, boolean parallel) {
        datalogMessagesList = new CopyOnWriteArrayList<Collection<IFacts>>();

        System.out.println("Convert to datalog message list with batch size " + batchSize);
        List<Collection<IFacts>> datalogMessages;
        if (parallel) {
            datalogMessages = transformJsonNodeToIFactsNative2(messagesList, batchSize);
        } else {
            datalogMessages = transformJsonNodeToIFactsNative(flatMessages, batchSize);
        }
        datalogMessagesList.addAll(datalogMessages);
        System.out.println("Message list size (JSON / DataIterator): " + messagesList.size() + " / " + datalogMessagesList.size());
    }

    public synchronized Iterator<Collection<IFacts>> getDatalogMessageListIterator() {
        return datalogMessagesList.iterator();
    }

    private ArrayList<JsonNode> parseAndTransformJsonToMessage(final File file, final ObjectReader om) throws IOException,
            JsonProcessingException {
        final JsonNode jsonNode = om.readTree(new FileInputStream(file));

        final ArrayList<JsonNode> messages = new ArrayList<JsonNode>();

        if (jsonNode.isArray()) {
            final ArrayNode messagesNode = (ArrayNode) jsonNode;
            for (final JsonNode node : messagesNode) {
                messages.add(node);
            }
        } else {
            throw new RuntimeException("Root node of message set file must be a array");
        }
        return messages;
    }

    // private List<com.github.dritter.hd.dlog.IFacts>
    // parseAndTransformJsonToDatalogMessage(final JsonFactory jsonFactory,
    // List<com.github.dritter.hd.dlog.IFacts> fs, final int arity) throws
    // JsonParseException, IOException {
    // final JsonParser jp = jsonFactory.createParser(MESSAGE_SET_FILE);

    // DataIterator ftIt = null;
    // ParameterValue<?>[] spv = null;
    // int counter = 0;

    // JsonToken token = null;
    // while ((token = jp.nextToken()) != null) {
    // if (jp.getCurrentTokenId() == ParseValues.START_OBJECT) {
    // counter = 0;

    // ftIt = new FillableTableIterator();
    // spv = new ParameterValue[arity];
    // } else if (jp.getCurrentTokenId() == ParseValues.VALUE_STRING) {
    // spv[counter] = ParameterValue.<String>create(jp.getText());
    // counter++;
    // } else if (jp.getCurrentTokenId() == ParseValues.VALUE_INT) {
    // spv[counter] = ParameterValue.<Integer>create(jp.getIntValue());
    // counter++;
    // } else if (jp.getCurrentTokenId() == ParseValues.VALUE_NUMBER_FLOAT) {
    // spv[counter] = ParameterValue.<Float>create(jp.getFloatValue());
    // counter++;
    // } else if (jp.getCurrentTokenId() == ParseValues.END_OBJECT) {
    // ((FillableTableIterator) ftIt).add(spv);
    // final String predicate = convertPredicateName("root");
    // final Facts newFact = Facts.create(Predicate.create(predicate, arity),
    // (ftIt));
    // fs.add(newFact);
    // }
    // }
    // jp.close();
    // return fs;
    // }

    private static String convertPredicateName(final String predicate) {
        return predicate.replaceAll("_", "-");
    }

    @Deprecated
    private static List<Collection<com.github.dritter.hd.dlog.IFacts>> transformJsonNodeToIFactsNative(final List<JsonNode> nodes,
            final int batchSize) {
        final List<Collection<com.github.dritter.hd.dlog.IFacts>> fs = new ArrayList<Collection<com.github.dritter.hd.dlog.IFacts>>(nodes.size()
                / batchSize);

        for (final JsonNode node : nodes) {
            final Iterator<String> fieldNames = node.fieldNames();

            // TODO: multi-format message; FIXME: what about multiple leading records? what about key relations between tables!!
            final Collection<DataIterator> subRelationFlIts = new ArrayList<DataIterator>(); // FIXME
            final DataIterator ftIt = buildDataIteratorFromJsonNode(node, fieldNames, subRelationFlIts);
            final String predicate = convertPredicateName("root");
            final Facts newFact = Facts.create(Predicate.create(predicate, node.size()), (ftIt));

            final Collection<com.github.dritter.hd.dlog.IFacts> subRelationCollection = new ArrayList<com.github.dritter.hd.dlog.IFacts>();
            final Iterator<DataIterator> subRelationIterator = subRelationFlIts.iterator();
            while (subRelationIterator.hasNext()) {
                final DataIterator nextSubRelation = subRelationIterator.next();
                subRelationCollection.add(Facts.create(Predicate.create(convertPredicateName("sub-root"), node.size()), (nextSubRelation)));
            }
            
            Collection<com.github.dritter.hd.dlog.IFacts> batchCollection = new ArrayList<com.github.dritter.hd.dlog.IFacts>(); // Collections.emptyList();
            fs.add(batchCollection);
            int batchCounter = 0; // batchSize;

            if (batchCounter - batchSize == 0) {
                if (batchCollection.size() != 0) {
                    fs.add(batchCollection);
                }
                batchCollection = new ArrayList<com.github.dritter.hd.dlog.IFacts>();
                batchCounter = 0;
            }
            batchCollection.add(newFact);
            batchCollection.addAll(subRelationCollection);
            batchCounter++;
        }
        return fs;
    }

    @Deprecated
    private static List<Collection<com.github.dritter.hd.dlog.IFacts>> transformJsonNodeToIFactsNative2(
            final Collection<Collection<JsonNode>> nodes, final int batchSize) {
        return nodes.parallelStream().map((batch) -> batch.stream().map((node) -> {
            final Iterator<String> fieldNames = node.fieldNames();

            final DataIterator ftIt = buildDataIteratorFromJsonNode(node, fieldNames);

            final String predicate = convertPredicateName("root");
            return Facts.create(Predicate.create(predicate, node.size()), (ftIt));
        }).collect(Collectors.<IFacts> toList())).collect(Collectors.<Collection<com.github.dritter.hd.dlog.IFacts>> toList());
    }

    private static DataIterator buildDataIteratorFromJsonNode(final JsonNode node, final Iterator<String> fieldNames) {
        return buildDataIteratorFromJsonNode(node, fieldNames, new ArrayList<DataIterator>());
    }

    private static DataIterator buildDataIteratorFromJsonNode(final JsonNode node, final Iterator<String> fieldNames,
            final Collection<DataIterator> subRelationFlIts) {
        final DataIterator ftIt = new FillableTableIterator();
        int counter = 0;

        final ParameterValue<?>[] spv = new ParameterValue[node.size()];
        while (fieldNames.hasNext()) {
            final String fieldName = fieldNames.next();
            final JsonNode jsonNode = node.get(fieldName);
            if (jsonNode.isDouble()) {
                spv[counter] = ParameterValue.<Double> create(jsonNode.asDouble());
            } else if (jsonNode.isTextual()) {
                final String text = jsonNode.asText();
                if (compiledRegex.matcher(text).matches()) {
                    try {
                        final Date date = format.get().parse(text);
                        final GregorianCalendar calendar = new GregorianCalendar();
                        calendar.setTime(date);
                        spv[counter] = ParameterValue.<Calendar> create(calendar);
                    } catch (final ParseException e) {
                        spv[counter] = ParameterValue.<String> create(text);
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    spv[counter] = ParameterValue.<String> create(text);
                }
            } else if (jsonNode.isInt()) {
                spv[counter] = ParameterValue.<Integer> create(jsonNode.asInt());
            } else if (jsonNode.isArray()) {
                System.out.println("Node " + fieldName + " is an array.");
                final ArrayNode subRelations = (ArrayNode) jsonNode;
                final Iterator<JsonNode> subRelationIterator = subRelations.iterator();
                while (subRelationIterator.hasNext()) {
                    final JsonNode subRelationNode = subRelationIterator.next();
                    final DataIterator subflIt = buildDataIteratorFromJsonNode(subRelationNode, subRelationNode.fieldNames(),
                            subRelationFlIts);
                    subRelationFlIts.add(subflIt);
                }
            } else {
                throw new IllegalArgumentException("Unsupported type: " + fieldName);
            }
            counter++;
        }
        ((FillableTableIterator) ftIt).add(spv);
        return ftIt;
    }
}
