package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OperatorDateBiggerThan implements Operator {
    private final String key;
    private Calendar value;

    public OperatorDateBiggerThan(final String key, final Date value) {
        this.key = key;

        this.value = Calendar.getInstance();
        this.value.setTime(value);
    }

    @Override
    public boolean eval(final JsonNode node) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String currentDate = node.get(key).asText();
        try {
            final Date date = sdf.parse(currentDate);
            final Calendar currentCalender = Calendar.getInstance();
            currentCalender.setTime(date);

            return currentCalender.after(value);
        } catch (final ParseException e) {
            throw new IllegalStateException("Could not parse date value: ", e);
        }
    }
}
