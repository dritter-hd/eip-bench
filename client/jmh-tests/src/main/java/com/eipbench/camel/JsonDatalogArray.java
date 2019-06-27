package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDatalogArray extends JsonDatalog {

    private JsonNode nestedJson;
    private boolean hasDirectValues = false;
    private static final transient Logger LOG = LoggerFactory.getLogger(JsonDatalogArray.class);

    public JsonDatalogArray(JsonNode nestedJson, String nameOfSuperObject, String parentId, boolean generateRandomIds,
                            boolean appendMissingVariables) {
        if (!nestedJson.isArray()) {
            throw new IllegalArgumentException("Nested json must be a JSON array");
        }

        this.nestedJson = nestedJson;
        this.nameOfSuperObject = nameOfSuperObject;
        this.parentId = parentId;
        this.generateRandomIds = generateRandomIds;
        idGenerator = new IdGenerator(generateRandomIds);
        this.uid = idGenerator.randomUUID();
        this.appendMissingVariables = appendMissingVariables;
    }

    @Override
    public void parseJson(DatalogProgramCreator creator) {
        DatalogFacts facts = creator.getFacts();
        DatalogMetaFacts metaFacts = creator.getMetaFacts();

        for (JsonNode object : nestedJson) {

            if (object.isArray()) {
                throw new NotYetImplementedException("Nested arrays in arrays are not supported yet");
            }

            if (object.isObject()) {
                saveNestedObject(object);
                continue;
            }

            hasDirectValues = true;
            createDatalogFactsForDirectValue(object, facts, metaFacts);
        }

        if (!hasDirectValues) {
            DatalogMetaFact metaFact = new DatalogMetaFact();
            DatalogFact fact = new DatalogFact();
            if (metaFacts.haveFactsFor(nameOfSuperObject)) {
                metaFact = metaFacts.getFactsForRelation(nameOfSuperObject);
                boolean parentIdInserted = false;
                for (String parameterName : metaFact.getParameterNameList()) {
                    if (parameterName.equals(ConversionHelper.UID_NAME)) {
                        fact.insertParamater(uid);
                    } else if (parameterName.equals(ConversionHelper.PARENT_ID_NAME)) {
                        fact.insertParamater(parentId);
                        parentIdInserted = true;
                    } else {
                        fact.insertParamater("null");
                        LOG.warn("Array without direct values has additional parameters in MetaFacts");
                    }
                }
                if (!parentIdInserted && appendMissingVariables) {
                    fact.insertParamater(parentId);
                    metaFact.addParameter(ConversionHelper.PARENT_ID_NAME);
                }
                facts.add(nameOfSuperObject, fact);
                metaFacts.add(nameOfSuperObject, metaFact);
            } else {
                if (appendMissingVariables) {
                    fact.insertParamater(uid);
                    fact.insertParamater(parentId);
                    metaFact.addParameter(ConversionHelper.UID_NAME);
                    metaFact.addParameter(ConversionHelper.PARENT_ID_NAME);
                    facts.add(nameOfSuperObject, fact);
                    metaFacts.add(nameOfSuperObject, metaFact);
                }
            }
        }
    }

    private void createDatalogFactsForDirectValue(JsonNode object, DatalogFacts facts, DatalogMetaFacts metaFacts) {
        DatalogFact fact = new DatalogFact();
        DatalogMetaFact metaFact = new DatalogMetaFact();

        if (metaFacts.haveFactsFor(nameOfSuperObject)) {
            metaFact = metaFacts.getFactsForRelation(nameOfSuperObject);
            createFactsAccordingToMetaFact(object, fact, metaFact);
        } else {
            fact.insertParamater(uid);
            fact.insertParamater(object.asText());
            metaFact.addParameter(ConversionHelper.UID_NAME);
            metaFact.addParameter(nameOfSuperObject);
            if (!parentId.isEmpty()) {
                fact.insertParamater(parentId);
                metaFact.addParameter(ConversionHelper.PARENT_ID_NAME);
            }
        }
        facts.add(nameOfSuperObject, fact);
        metaFacts.add(nameOfSuperObject, metaFact);
    }

    /**
     * Creates facts according to metaFacts Due to the fact that facts for array
     * direct values should have an arity of 3 (uid, object, parentID) a warning
     * is logged, when there are other parameterNames in the MetaFacts, however
     * still null is stored. In case there is no parameterName liked the
     * superObject (in which the direct value is normally stored), a new one is
     * created
     * 
     * @param object
     * @param fact
     * @param metaFact
     */
    private void createFactsAccordingToMetaFact(JsonNode object, DatalogFact fact, DatalogMetaFact metaFact) {

        boolean objectIsIncludedInCreatedFact = false;
        boolean parentIdInserted = false;
        for (String parameterName : metaFact.getParameterNameList()) {
            boolean isUidParameter = parameterName.equals(ConversionHelper.UID_NAME);
            boolean isParentIdParameter = parameterName.equals(ConversionHelper.PARENT_ID_NAME);
            if (isParentIdParameter) {
                fact.insertParamater(parentId);
                parentIdInserted = true;
            } else if (isUidParameter) {
                fact.insertParamater(uid);
            } else if (parameterName.equals(nameOfSuperObject)) {
                fact.insertParamater(object.asText());
                objectIsIncludedInCreatedFact = true;
            } else {
                fact.insertParamater("null");
                LOG.warn("Direct values in array named: " + nameOfSuperObject + ", but metaFacts also have additional fields.");
            }
        }

        if (!objectIsIncludedInCreatedFact) {
            fact.insertParamater(object.asText());
            metaFact.addParameter(nameOfSuperObject);
        }

        if ((!parentIdInserted) && (!parentId.isEmpty())) {
            fact.insertParamater(parentId);
            metaFact.addParameter(ConversionHelper.PARENT_ID_NAME);
        }
    }

    private void saveNestedObject(JsonNode object) {
        JsonDatalogObject jsonDatalogObject = new JsonDatalogObject(object, nameOfSuperObject + "-subObj", uid, generateRandomIds,
                appendMissingVariables);
        nestedObjects.add(jsonDatalogObject);
    }
}
