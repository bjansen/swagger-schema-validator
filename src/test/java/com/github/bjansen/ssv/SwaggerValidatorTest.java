package com.github.bjansen.ssv;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import io.swagger.util.Json;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

class SwaggerValidatorTest {

    @Test
    void checkOneOf() throws IOException, ProcessingException {
        SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

        InputStreamReader sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/valid.json"));
        ProcessingReport report = validator.validate(CharStreams.toString(sample), "/definitions/User");
        assertTrue(report.isSuccess());
        ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
        assertEquals(0, messages.size());

        sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/valid2.json"));
        report = validator.validate(CharStreams.toString(sample), "/definitions/User");
        assertTrue(report.isSuccess());

        sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/invalid.json"));
        report = validator.validate(CharStreams.toString(sample), "/definitions/User");
        assertFalse(report.isSuccess());
        messages = ImmutableList.copyOf(report);
        assertEquals(1, messages.size());
        assertEquals("instance failed to match exactly one schema (matched 2 out of 2)", messages.get(0).getMessage());
    }

    @Test
    void checkInt64Validation() throws IOException, ProcessingException {
        SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

        InputStreamReader sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/invalid2.json"));
        ProcessingReport report = validator.validate(CharStreams.toString(sample), "/definitions/User");

        assertTrue(report.isSuccess());
        ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
        assertEquals(1, messages.size());

        ProcessingMessage message = messages.get(0);
        assertEquals(LogLevel.WARNING, message.getLogLevel());
        assertTrue(message.getMessage().contains("value for int64 leads to overflow (found: 9999"));
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
    void should_throw_IOException_when_payload_is_empty() throws IOException {
        // Given
        SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

        // When
        final Executable executable = () -> validator.validate("", "/definitions/User");

        // Then
        assertThrows(IOException.class, executable);
    }

    @Test
    void should_accept_valid_document_when_deepCheck_is_true() throws IOException, ProcessingException {
        // Given
        SwaggerValidator validator = buildValidator("/deepCheck/spec.yaml");
        JsonNode sample = buildSample("/deepCheck/valid.json");

        // When
        ProcessingReport report = validator.validate(sample, "/definitions/User", true);

        // Then
        assertTrue(report.isSuccess());
    }

    @Test
    void should_report_nested_errors_when_deepCheck_is_true() throws IOException, ProcessingException {
        // Given
        SwaggerValidator validator = buildValidator("/deepCheck/spec.yaml");
        JsonNode sample = buildSample("/deepCheck/invalid.json");

        // When
        ProcessingReport report = validator.validate(sample, "/definitions/User", true);

        // Then
        assertFalse(report.isSuccess());

        final ArrayList<ProcessingMessage> exceptions = new ArrayList<>();
        report.iterator().forEachRemaining(exceptions::add);

        assertEquals(2, exceptions.size());
    }

    @Test
    void should_not_report_nested_errors_when_deepCheck_is_false() throws IOException, ProcessingException {
        // Given
        SwaggerValidator validator = buildValidator("/deepCheck/spec.yaml");
        JsonNode sample = buildSample("/deepCheck/invalid.json");

        // When
        ProcessingReport report = validator.validate(sample, "/definitions/User", false);

        // Then
        assertFalse(report.isSuccess());

        final ArrayList<ProcessingMessage> exceptions = new ArrayList<>();
        report.iterator().forEachRemaining(exceptions::add);

        assertEquals(1, exceptions.size());
    }

    private SwaggerValidator buildValidator(String pathToSpec) throws IOException {
        InputStream spec = getClass().getResourceAsStream(pathToSpec);
        return SwaggerValidator.forYamlSchema(new InputStreamReader(spec));
    }

    private JsonNode buildSample(String s) throws IOException {
        return Json.mapper().readTree(new InputStreamReader(getClass().getResourceAsStream(s)));
    }
}
