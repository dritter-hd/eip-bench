package com.eipbench.states.fast;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class InlineMessageParser {
    public interface MessageHandler {
        public void handle(JsonNode message);
    }

    public void parse(File f, MessageHandler handler) throws IOException {
        ObjectMapper mp = new ObjectMapper();
        JsonParser parser = new JsonFactory(mp).createParser(f);
        while(parser.nextToken() != null) {
            JsonToken currentToken = parser.getCurrentToken();

            if (currentToken == JsonToken.START_OBJECT) {
                JsonNode message = parser.readValueAsTree();

                handler.handle(message);
            }
        }
    }
}
