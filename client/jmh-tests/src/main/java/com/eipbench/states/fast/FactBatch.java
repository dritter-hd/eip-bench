package com.eipbench.states.fast;

import com.github.dritter.hd.dlog.Facts;
import com.github.dritter.hd.dlog.IFacts;
import com.github.dritter.hd.dlog.Predicate;
import com.github.dritter.hd.dlog.algebra.DataIterator;
import com.github.dritter.hd.dlog.algebra.FillableTableIterator;
import com.github.dritter.hd.dlog.algebra.ParameterValue;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FactBatch implements BytesMarshallable {
    private static ArrayList<IFacts> factBatch = new ArrayList<>(0);

    final static short FACT = 0;
    final static short END_FACTS = 1;

    final static short VALUE_ARRAY = 2;
    final static short END_VALUE_ARRAY= 3;

    final static short VALUE = 4;
    final static short END_VALUE = 5;

    final static short BOOLEAN = 6;
    final static short DOUBLE = 7;
    final static short INT = 8;
    final static short LONG = 9;
    final static short TEXTUAL = 10;
    final static short CALENDAR = 11;

    public void setFacts(ArrayList<IFacts> facts) {
        this.factBatch = facts;
    }

    public ArrayList<IFacts> getFacts() {
        return factBatch;
    }

    @Override
    public void readMarshallable(Bytes bytes) throws IllegalStateException {
        int batchSize = bytes.readInt();

        if (factBatch.size() != batchSize) {
            initialize(batchSize);
        }

        int j=0;
        while(bytes.readShort() != END_FACTS) {
            int predicateArity = bytes.readInt();
            String predicateName = readString(bytes);

            final FillableTableIterator ftIt = new FillableTableIterator();
            while (bytes.readShort() != END_VALUE_ARRAY) {
                ParameterValue<?>[] values = new ParameterValue[bytes.readInt()];
                int i=0;
                while (bytes.readShort() != END_VALUE) {
                    short type = bytes.readShort();

                    if (type == TEXTUAL) {
                        values[i] = ParameterValue.<String>create(readString(bytes));
                    } else if (type == BOOLEAN) {
                        values[i] = ParameterValue.<Boolean>create(bytes.readBoolean());
                    } else if (type == DOUBLE) {
                        values[i] = ParameterValue.<Double>create(bytes.readDouble());
                    } else if (type == INT) {
                        values[i] = ParameterValue.<Integer>create(bytes.readInt());
                    } else if (type == CALENDAR) {
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.setTimeInMillis(bytes.readLong());
                        values[i] = ParameterValue.<Calendar>create(cal);
                    }

                    i++;
                }
                ftIt.add(values);
            }

            factBatch.set(j, Facts.create(Predicate.create(predicateName, predicateArity), ftIt));
            j++;
        }
    }

    private void initialize(int batchSize) {
        if (factBatch.size() != batchSize) {
            factBatch = new ArrayList<>(batchSize);
            for(int i = 0; i < batchSize; i++) {
                factBatch.add(null);
            }
        }
    }

    @Override
    public void writeMarshallable(Bytes bytes) {
        bytes.writeInt(factBatch.size());

        for (IFacts iFacts : factBatch) {
            bytes.writeShort(FACT);

            /* Predicate */
            Predicate predicate = iFacts.getPredicate();
            bytes.writeInt(predicate.getArity());
            writeString(bytes, predicate.getName());

            /* Values */
            DataIterator values = iFacts.getValues();

            if (values instanceof FillableTableIterator) {
                values.open();
                ParameterValue<?>[] next;
                while((next = values.next()) != null) {
                    bytes.writeShort(VALUE_ARRAY);
                    bytes.writeInt(next.length);

                    for (ParameterValue<?> parameterValue : next) {
                        bytes.writeShort(VALUE);

                        Object value = parameterValue.get();

                        if (value instanceof Integer) {
                            bytes.writeShort(INT);
                            bytes.writeInt((Integer)value);
                        } else if (value instanceof String) {
                            bytes.writeShort(TEXTUAL);
                            writeString(bytes,(String)value);
                        } else if (value instanceof Double) {
                            bytes.writeShort(DOUBLE);
                            bytes.writeDouble((Double) value);
                        } else if (value instanceof Calendar) {
                            bytes.writeShort(CALENDAR);
                            long timeInMillis = ((Calendar) value).getTimeInMillis();
                            bytes.writeLong(timeInMillis);
                        } else {
                            throw new RuntimeException("Value can't be serialized: " + value + " (" + value.getClass() + ")");
                        }
                    }
                    bytes.writeShort(END_VALUE);
                }
                bytes.writeShort(END_VALUE_ARRAY);
            } else {
                throw new RuntimeException("Currently only fillable table iterators can be serialized");
            }
        }
        bytes.writeShort(END_FACTS);
    }

    private String readString(Bytes bytes) {
        int length = bytes.readInt();
        byte[] byteString = new byte[length];

        if (length > 1000) {
            throw new IllegalStateException("String length out of bounds");
        }

        bytes.read(byteString);
        return new String(byteString);
    }

    private void writeString(Bytes bytes, String string) {
        byte[] stringBytes = string.getBytes();
        bytes.writeInt(stringBytes.length);
        bytes.write(stringBytes);
    }
}
