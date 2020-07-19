package com.github.bjansen.ssv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.JsonReferenceException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.ImmutableMap;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class SwaggerValidator {

    private static final Map<Pair<JsonNode, String>, JsonSchema> SCHEMA_CACHE = new HashMap<>();

    private static final Map<String, String> TRANSFORMATIONS =
        ImmutableMap.<String, String>builder()
            .put("x-additionalItems", "additionalItems")
            .put("x-contains", "contains")
            .put("x-patternProperties", "patternProperties")
            .put("x-dependencies", "dependencies")
            .put("x-propertyNames", "propertyNames")
            .put("x-if", "if")
            .put("x-then", "then")
            .put("x-else", "else")
            .put("x-allOf", "allOf")
            .put("x-anyOf", "anyOf")
            .put("x-oneOf", "oneOf")
            .put("x-not", "not")
            .build();

    private final JsonNode schemaObject;

    private SwaggerValidator(JsonNode schemaObject) {
        this.schemaObject = transform(schemaObject);
    }

    /**
     * Creates a Swagger schema validator based on the given JSON-based Swagger spec.
     *
     * @param swaggerSpec the Swagger spec (in JSON format)
     * @return a validator for that spec
     * @throws IOException if the Swagger spec is not a valid JSON object
     */
    public static SwaggerValidator forJsonSchema(Reader swaggerSpec) throws IOException {
        return new SwaggerValidator(Json.mapper().readTree(swaggerSpec));
    }

    /**
     * Creates a Swagger schema validator based on the given YAML-based Swagger spec.
     *
     * @param swaggerSpec the Swagger spec (in YAML format)
     * @return a validator for that spec
     * @throws IOException if the Swagger spec is not a valid YAML object
     */
    public static SwaggerValidator forYamlSchema(Reader swaggerSpec) throws IOException {
        return new SwaggerValidator(Yaml.mapper().readTree(swaggerSpec));
    }

    /**
     * Validates the given {@code jsonPayload} against the definition located at {@code definitionPointer}.
     *
     * @param jsonPayload       the JSON payload to validate
     * @param definitionPointer the path to the schema object the payload should be validated against,
     *                          for example {@code /definitions/User}
     * @return a validation report
     * @throws ProcessingException in case a processing error occurred during validation
     * @throws IOException         if the payload is not a valid JSON object
     */
    public ProcessingReport validate(String jsonPayload, String definitionPointer) throws ProcessingException, IOException {
        if (jsonPayload == null || jsonPayload.equals("")) {
            throw new IOException("Payload is empty");
        }
        return validate(jsonPayload, definitionPointer, Json.mapper());
    }

    /**
     * Same as {@link #validate(String, String)} but with a custom JSON deserializer.
     */
    public ProcessingReport validate(String jsonPayload, String definitionPointer, ObjectMapper jsonMapper) throws ProcessingException, IOException {
        JsonNode jsonNode = jsonMapper.readTree(jsonPayload);
        if (jsonNode == null) {
            throw new IOException("The JSON payload could not be parsed correctly");
        }

        return validate(jsonNode, definitionPointer);
    }

    /**
     * Same as {@link #validate(JsonNode, String, boolean)} with {@code deepCheck = false}.
     */
    public ProcessingReport validate(JsonNode jsonPayload, String definitionPointer) throws ProcessingException {
        return validate(jsonPayload, definitionPointer, false);
    }

    /**
     * Validates the given {@code jsonPayload} against the definition located at {@code definitionPointer}.
     *
     * @param jsonPayload       the JSON payload (as a JsonNode) to validate
     * @param definitionPointer the path to the schema object the payload should be validated against,
     *                          for example {@code /definitions/User}
     * @param deepCheck         validate children even if the container (array, object) is invalid
     * @return a validation report
     * @throws ProcessingException in case a processing error occurred during validation
     */
    public ProcessingReport validate(JsonNode jsonPayload, String definitionPointer, boolean deepCheck) throws ProcessingException {
        return getSchema(definitionPointer).validate(jsonPayload, deepCheck);
    }

    /**
     * Applies all the {@link #TRANSFORMATIONS} on each property contained in each definition
     * of the given schema.
     *
     * @param schema the Swagger schema containing transformations to apply (x-oneOf, etc).
     * @return the patched schema
     */
    private JsonNode transform(JsonNode schema) {
        if (!(schema instanceof ObjectNode)) {
            return schema;
        }

        ObjectNode schemaNode = (ObjectNode) schema;

        if (schemaNode.has("definitions")) {
            for (JsonNode definition : schemaNode.get("definitions")) {
                TRANSFORMATIONS.forEach((from, to) -> {
                    if (definition instanceof ObjectNode && definition.has(from)) {
                        ((ObjectNode) definition).set(to, definition.get(from));
                        ((ObjectNode) definition).remove(from);
                    }
                });
            }
        }

        return schema;
    }

    private JsonSchema getSchema(String definitionPointer) throws ProcessingException {
        Pair<JsonNode, String> key = Pair.of(schemaObject, definitionPointer);

        JsonSchemaFactory jsonSchemaFactory = SwaggerV20Library.schemaFactory(LogLevel.INFO, LogLevel.FATAL);

        if (!SCHEMA_CACHE.containsKey(key)) {
            try {
                SCHEMA_CACHE.put(key, jsonSchemaFactory.getJsonSchema(schemaObject, definitionPointer));
            } catch (JsonReferenceException e) {
                throw new ProcessingException("Unknown definition " + definitionPointer, e);
            }
        }

        return SCHEMA_CACHE.get(key);
    }
}
