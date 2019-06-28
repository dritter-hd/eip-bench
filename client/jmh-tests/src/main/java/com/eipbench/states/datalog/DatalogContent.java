package com.eipbench.states.datalog;

import com.eipbench.content.Content;
import com.eipbench.states.fast.FastDatalogMessageBatchSet;
import com.github.dritter.hd.dlog.IFacts;
import com.eipbench.content.OrderMessageSet;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.util.Collection;
import java.util.Iterator;

public class DatalogContent extends Content {
    private Iterator<Collection<IFacts>> iterator;
    private Collection<IFacts> message;

    public FastDatalogMessageBatchSet fastOrderMessageSet = new FastDatalogMessageBatchSet("order-messages-datalog", OrderMessageSet.MESSAGE_SET_FILE, 1500000);

    @Setup(Level.Trial)
    public void parseDatalog() {
        if (!offHeapMessages) {
            messageSet.parse(batchSize, msgScaleLevel);
            messageSet.parseDatalog(batchSize, false);
        } else {
            fastOrderMessageSet.load(batchSize);
        }
    }

    @Setup(Level.Iteration)
    public void prepareIterator() {
        if (!offHeapMessages) {
            iterator = messageSet.getDatalogMessageListIterator();
        } else {
            fastOrderMessageSet.reset();
        }
    }

    @Setup(Level.Invocation)
    public void prepareMessage() {
        if (!offHeapMessages) {
            if (!iterator.hasNext()) {
                iterator = messageSet.getDatalogMessageListIterator();
            }

            message = iterator.next();
        } else {
            message = fastOrderMessageSet.getNextBatch();
        }
    }

    public Collection<IFacts> getMessage() {
        return message;
    }
}
