package com.eipbench.states.fast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO: Does not support nested object nodes yet
  */
public class ObjectNodeBatch implements BytesMarshallable{
    private ArrayList<JsonNode> objectNodes;

    private static ObjectMapper mapper = new ObjectMapper();

    public ObjectNodeBatch() {

    }

    final static short BOOLEAN = 0;
    final static short DOUBLE = 1;
    final static short INT = 2;
    final static short LONG = 3;
    final static short TEXTUAL = 4;

    final static short FIELD = 5;
    final static short EOF = 6;

    final static short OBJECT = 7;
    final static short EOO = 8;

    @Override
    public void readMarshallable(Bytes bytes) throws IllegalStateException {
        int batchSize = bytes.readInt();

        objectNodes = new ArrayList<>(batchSize);

        while(bytes.readShort() != EOO) {
            ObjectNode objectNode = mapper.createObjectNode();

            while (bytes.readShort() != EOF) {
                String fieldName = readString(bytes);

                short type = bytes.readShort();
                if (type == TEXTUAL) {
                    objectNode.put(fieldName, readString(bytes));
                } else if (type == BOOLEAN) {
                    objectNode.put(fieldName, bytes.readBoolean());
                } else if (type == DOUBLE) {
                    objectNode.put(fieldName, bytes.readDouble());
                } else if (type == INT) {
                    objectNode.put(fieldName, bytes.readInt());
                } else if (type == LONG) {
                    objectNode.put(fieldName, bytes.readLong());
                }
            }

            objectNodes.add(objectNode);
        }
    }

    /*private void initialize(int batchSize) {
        if (objectNodes.get().size() != batchSize) {
            objectNodes.set(new ArrayList<>(batchSize));
            for (int i = 0; i < batchSize; i++) {
                objectNodes.get().add(mapper.createObjectNode());
            }
        }
    }*/

    private String readString(Bytes bytes) {
        int length = bytes.readInt();
        byte[] byteString = new byte[length];
        bytes.read(byteString);
        return new String(byteString);
    }

    @Override
    public void writeMarshallable(Bytes bytes) {
        bytes.writeInt(objectNodes.size());

        for (JsonNode objectNode : objectNodes) {
            bytes.writeShort(OBJECT);

            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while(fields.hasNext()) {
                bytes.writeShort(FIELD);

                Map.Entry<String, JsonNode> next = fields.next();
                byte[] textBytesKey = next.getKey().getBytes();
                bytes.writeInt(textBytesKey.length);
                bytes.write(textBytesKey);
                JsonNode value = next.getValue();
                JsonNodeType nodeType = value.getNodeType();

                if (nodeType == JsonNodeType.BOOLEAN) {
                    bytes.writeShort(BOOLEAN);
                    bytes.writeBoolean(value.asBoolean());
                } else if (value.isDouble()) {
                    bytes.writeShort(DOUBLE);
                    bytes.writeDouble(value.asDouble());
                } else if (value.isInt()) {
                    bytes.writeShort(INT);
                    bytes.writeInt(value.asInt());
                } else if (value.isLong()) {
                    bytes.writeShort(LONG);
                    bytes.writeLong(value.asLong());
                } else if (value.isTextual()) {
                    bytes.writeShort(TEXTUAL);
                    byte[] textBytes = value.asText().getBytes();
                    bytes.writeInt(textBytes.length);
                    bytes.write(textBytes);
                }
            }
            bytes.writeShort(EOF);
        }

        bytes.writeShort(EOO);
    }

    public void setObjectNode(ArrayList<JsonNode> objectNode) {
        this.objectNodes = objectNode;
    }

    public ArrayList<JsonNode> getObjectNode() {
        return objectNodes;
    }
}
