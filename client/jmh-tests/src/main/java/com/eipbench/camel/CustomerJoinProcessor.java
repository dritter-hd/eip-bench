package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.eipbench.camel.CustomerNationRegionEmbeddedMessageSet.CustomerNames;
import com.eipbench.camel.CustomerNationRegionMultiformatMessageSet.NationNames;
import com.eipbench.camel.CustomerNationRegionMultiformatMessageSet.RegionNames;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/* For Speed Optimisation: The first Node inside the Array must be "CUSTOMER".
 * After that all "NATION" nodes must appear.
 * The "REGION" nodes must be at the very end.
 */
public class CustomerJoinProcessor extends JavaJsonNodeProcessor {
    private final static JsonNodeFactory jsonFactory = new JsonNodeFactory(false);
    private final static int CustomerNodePosition = 0;

    private ObjectNode shallowCopy(JsonNode from) {
        ObjectNode aNode = new ObjectNode(jsonFactory);
        Iterator<Entry<String, JsonNode>> iterator = from.fields();
        while (iterator.hasNext()) {
            Entry<String, JsonNode> aEntry = iterator.next();
            String aName = aEntry.getKey();
            if (!aName.equals(CustomerNames.NAME.toString()) && !aName.equals(CustomerNames.TYPE.toString())) {
                aNode.put(aName, aEntry.getValue());
            }
        }
        return aNode;
    }

    private ArrayNode createArrayNode(JsonNode anode) {
        ArrayNode result = new ArrayNode(jsonFactory);
        ObjectNode nationnode = shallowCopy(anode);
        result.add(nationnode);
        return result;
    }

    @Override
    public void processNode(JsonNode jsonNode, List<JsonNode> result) {
        final ArrayNode arrayNode = (ArrayNode) jsonNode;
        final JsonNode customernode = arrayNode.get(CustomerNodePosition);
        final ObjectNode newNode = shallowCopy(customernode);
        ArrayNode nationarray;
        ObjectNode nationnode = null;
        ArrayNode regionarray;
        int nationkey = customernode.get(CustomerNames.C_NATIONKEY.toString()).asInt();
        int regionkey = -1;

        if (nationkey < 0) {throw new IllegalArgumentException("nationkey not found");};

        for (int i = 1; i < arrayNode.size(); i++) {
            JsonNode jn = arrayNode.get(i);
            String type = jn.get(CustomerNames.TYPE.toString()).asText();
            switch(type) {
            case "NATION":
                if (jn.get(NationNames.N_NATIONKEY.toString()).asInt() == nationkey) {
                    regionkey = jn.get(NationNames.N_REGIONKEY.toString()).asInt();
                    nationarray = createArrayNode(jn);
                    nationnode = (ObjectNode) nationarray.get(0);
                    newNode.put("NATION", nationarray);
                }
                break;
            case "REGION":
                if (jn.get(RegionNames.R_REGIONKEY.toString()).asInt() == regionkey) {
                    regionarray = createArrayNode(jn);
                    if (nationnode == null) {
                        throw new IllegalArgumentException("NationNode not found");
                    } else {
                        nationnode.put("REGION", regionarray);
                    }
                }
                break;
            }
        }
        result.add(newNode);
    }
}