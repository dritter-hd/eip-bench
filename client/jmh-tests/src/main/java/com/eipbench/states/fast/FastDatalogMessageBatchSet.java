package com.eipbench.states.fast;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dritter.hd.dlog.Facts;
import com.github.dritter.hd.dlog.IFacts;
import com.github.dritter.hd.dlog.Predicate;
import com.github.dritter.hd.dlog.algebra.DataIterator;
import com.github.dritter.hd.dlog.algebra.FillableTableIterator;
import com.github.dritter.hd.dlog.algebra.ParameterValue;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class FastDatalogMessageBatchSet extends FastMessageBatchSet {
    private final int totalMessageCount;
    ChronicleMap<Integer, FactBatch> batches;

    private static String regex = "^((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";
    private static Pattern compiledRegex = Pattern.compile(regex);
    private static ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("YYYY-MM-dd");
        }
    };

    public FastDatalogMessageBatchSet(String name, File allMessagesJsonFile, int totalMessageCount) {
        super(name, allMessagesJsonFile);
        this.totalMessageCount = totalMessageCount;
    }

    protected void createBatchMap(int batchSize, File mmapFile) throws IOException {
        batches = ChronicleMapBuilder.of(Integer.class, FactBatch.class)
                .entries(totalMessageCount/batchSize)
                .averageValueSize(800*batchSize)
                .createPersistedTo(mmapFile);
    }

    protected Batcher<IFacts> createBatcher(final int batchSize) {
        return new Batcher<IFacts>(batchSize) {
            private String convertPredicateName(final String predicate) {
                return predicate.replaceAll("_", "-");
            }

            @Override
            protected void addBatch(ArrayList<IFacts> currentBatch) {
                FactBatch factBatch = new FactBatch();
                factBatch.setFacts(currentBatch);
                batches.put(allBatchCounter, factBatch);
            }

            @Override
            protected IFacts convertMessage(JsonNode node) {
                final Iterator<String> fieldNames = node.fieldNames();

                final DataIterator ftIt = new FillableTableIterator();
                int counter = 0;
                final ParameterValue<?>[] spv = new ParameterValue[node.size()];
                while (fieldNames.hasNext()) {
                    final String next = fieldNames.next();
                    final JsonNode jsonNode = node.get(next);
                    if (jsonNode.isDouble()) {
                        spv[counter] = ParameterValue.<Double>create(jsonNode.asDouble());
                    } else if (jsonNode.isTextual()) {
                        final String text = jsonNode.asText();
                        if (compiledRegex.matcher(text).matches()) {
                            try {
                                final Date date = format.get().parse(text);
                                final GregorianCalendar calendar = new GregorianCalendar();
                                calendar.setTime(date);
                                spv[counter] = ParameterValue.<Calendar>create(calendar);
                            } catch (final ParseException e) {
                                spv[counter] = ParameterValue.<String>create(text);
                            } catch (final NumberFormatException e) {

                            }
                        } else {
                            spv[counter] = ParameterValue.<String>create(text);
                        }
                    } else if (jsonNode.isInt()) {
                        spv[counter] = ParameterValue.<Integer>create(jsonNode.asInt());
                    } else {
                        throw new IllegalArgumentException("Unsupported type: " + next);
                    }
                    counter++;
                }
                ((FillableTableIterator) ftIt).add(spv);

                final String predicate = convertPredicateName("root");
                return Facts.create(Predicate.create(predicate, node.size()), (ftIt));
            }
        };
    }

    private FactBatch batch = new FactBatch();
    public ArrayList<IFacts> getNextBatch() {
        return batches.getUsing(iterate(), batch).getFacts();
    }
}
