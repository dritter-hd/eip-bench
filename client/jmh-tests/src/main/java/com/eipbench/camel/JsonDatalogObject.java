package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class JsonDatalogObject extends JsonDatalog {

    private ObjectNode nestedJson;
    private static final transient Logger LOG = LoggerFactory.getLogger(JsonDatalogObject.class);

    public JsonDatalogObject(JsonNode nestedJson, String nameOfSuperObject, String parentId, boolean generateRandomIds,
                             boolean appendMissingVariables) {

        if (!nestedJson.isObject()) {
            throw new IllegalArgumentException("nestedJson must be a JSON object");
        }

        this.nestedJson = (ObjectNode)nestedJson;
        this.nameOfSuperObject = nameOfSuperObject;
        this.parentId = parentId;
        this.generateRandomIds = generateRandomIds;
        idGenerator = new IdGenerator(generateRandomIds);
        this.uid = idGenerator.randomUUID();
        this.appendMissingVariables = appendMissingVariables;
    }

    public JsonNode getNestedJSON() {
        return nestedJson;
    }

    @Override
    public void parseJson(final DatalogProgramCreator creator) {
        DatalogFacts facts = creator.getFacts();
        DatalogMetaFacts metaFacts = creator.getMetaFacts();

        DatalogFact fact = new DatalogFact();
        DatalogMetaFact metaFact = new DatalogMetaFact();

        nestedJson.put(ConversionHelper.UID_NAME, uid);

        if (parentId != null) {
            nestedJson.put(ConversionHelper.PARENT_ID_NAME, parentId);
        }

        if (metaFacts.haveFactsFor(nameOfSuperObject)) {
            metaFact = metaFacts.getFactsForRelation(nameOfSuperObject);

            for (String parameterName : metaFact.getParameterNameList()) {
                if (nestedJson.has(parameterName)) {
                    JsonNode attributeValue = nestedJson.get(parameterName);
                    fact.insertParamater(attributeValue.asText());
                    nestedJson.remove(parameterName);
                } else {
                    fact.insertParamater("null");
                    nestedJson.remove(parameterName);
                }
            }
        }

        // cover leftovers if appendMissingVariables is activated or there are
        // no metafacts for the relation called ${nameOfSuperObject} so far
        Iterator<?> keys = nestedJson.fieldNames();
        while (keys.hasNext()) {
            String key = (String) keys.next();

            if (nestedJson.get(key).isArray()) {
                saveNestedArray(key);
                continue;
            }

            if (nestedJson.get(key).isObject()) {
                saveNestedObject(key);
                continue;
            }

            if (appendMissingVariables) {
                final JsonNode parameterValue = nestedJson.get(key);
                if (parameterValue.isBoolean()) {
                    fact.insertParamater(parameterValue.asBoolean());
                } else {
                    fact.insertParamater(parameterValue.asText());
                }

                metaFact.addParameter(key);
            }
        }

        if (appendMissingVariables) {
            if (parentId != null) {
                if (!metaFact.getParameterNameList().contains(ConversionHelper.PARENT_ID_NAME)) {
                    metaFact.addParameter(ConversionHelper.PARENT_ID_NAME);
                }
            }
        }

        if (!fact.isEmpty()) {
            facts.add(nameOfSuperObject, fact);
            if (!metaFacts.haveFactsFor(nameOfSuperObject)) {
                metaFacts.add(nameOfSuperObject, metaFact);
            }
        } else {
            LOG.warn("Empty facts for "+nameOfSuperObject);
        }
    }

    private void saveNestedArray(String key) {
        JsonDatalogArray jsonDatalogArray = new JsonDatalogArray(nestedJson.get(key), key, uid, generateRandomIds, appendMissingVariables);
        nestedObjects.add(jsonDatalogArray);
    }

    private void saveNestedObject(String key) {
        JsonDatalogObject jsonDatalogObject = new JsonDatalogObject(nestedJson.get(key), key, uid, generateRandomIds, appendMissingVariables);
        nestedObjects.add(jsonDatalogObject);
    }
}
