package com.github.bjansen.ssv;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import io.swagger.util.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

class SwaggerValidatorTest {

    @Test
    void checkOneOf() throws IOException, ProcessingException {
        InputStream spec = getClass().getResourceAsStream("/oneOf/spec.yaml");
        SwaggerValidator validator = SwaggerValidator.forYamlSchema(new InputStreamReader(spec));

        InputStreamReader sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/valid.json"));
        ProcessingReport report = validator.validate(CharStreams.toString(sample), "/definitions/User");
        assertTrue(report.isSuccess());

        sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/valid2.json"));
        report = validator.validate(CharStreams.toString(sample), "/definitions/User");
        assertTrue(report.isSuccess());

        sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/invalid.json"));
        report = validator.validate(CharStreams.toString(sample), "/definitions/User");
        assertFalse(report.isSuccess());
        ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
        assertEquals(1, messages.size());
        assertEquals("instance failed to match exactly one schema (matched 2 out of 2)", messages.get(0).getMessage());
    }

    @Test
    void checkTransformations() throws IOException {
        InputStream spec = getClass().getResourceAsStream("/transformations/spec-before.json");
        SwaggerValidator validator = SwaggerValidator.forJsonSchema(new InputStreamReader(spec));

        JsonNode schemaObject = (JsonNode) readFieldValue(SwaggerValidator.class, "schemaObject", validator)
            .orElse(null);
        assertNotNull(schemaObject);

        assertEquals(Json.mapper().readTree(getClass().getResourceAsStream("/transformations/spec-after.json")),
            schemaObject);
    }

    @Test
    void checkEmptyPayload() throws IOException {
        InputStream spec = getClass().getResourceAsStream("/oneOf/spec.yaml");
        SwaggerValidator validator = SwaggerValidator.forYamlSchema(new InputStreamReader(spec));

        assertThrows(IOException.class, () -> validator.validate("", "/definitions/User"));
    }
}
