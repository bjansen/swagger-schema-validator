package com.github.bjansen.ssv;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import io.swagger.util.Json;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

class SwaggerValidatorTest {

    @Nested
    class GeneralUsage {
        @Test
        void should_throw_when_payload_is_null() throws IOException {
            // Given
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            // When
            final Executable executable = () -> validator.validate((String) null, "/definitions/User");

            // Then
            assertThrows(IOException.class, executable);
        }

        @Test
        void should_throw_when_payload_is_NOT_AVAILABLE() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");
            JsonNode payload = new ObjectMapper().readTree("");

            // When
            ProcessingReport report = validator.validate(payload, "/definitions/User");

            // Then
            assertFalse(report.isSuccess());
            assertTrue(report.toString().contains("Payload is empty"));
        }

        @Test
        void should_throw_when_payload_is_empty() throws IOException {
            // Given
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            // When
            final Executable executable = () -> validator.validate("", "/definitions/User");

            // Then
            assertThrows(IOException.class, executable);
        }

        @Test
        void should_throw_when_payload_is_invalid() throws IOException {
            // Given
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            // When
            final Executable executable = () -> validator.validate("brrrrr", "/definitions/User");

            // Then
            assertThrows(JsonParseException.class, executable);
        }

        @Test
        void should_throw_when_validating_with_custom_mapper_and_null_payload() throws IOException {
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            Executable exec = () -> validator.validate(null, "/definitions/User", Json.mapper());

            assertThrows(IOException.class, exec);
        }

        @Test
        void should_throw_when_validating_with_custom_mapper_and_empty_payload() throws IOException {
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            Executable exec = () -> validator.validate("", "/definitions/User", Json.mapper());

            assertThrows(IOException.class, exec);
        }

        @Test
        void should_throw_when_validating_with_custom_mapper_and_invalid_payload() throws IOException {
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            Executable exec = () -> validator.validate("brrrrr", "/definitions/User", Json.mapper());

            assertThrows(JsonParseException.class, exec);
        }

        @Test
        void should_report_unknown_definition_without_crashing() throws IOException {
            // Given
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            // When
            final Executable executable = () -> validator.validate("{}", "/definitions/Blablabla");

            // Then
            Exception e = assertThrows(ProcessingException.class, executable);
            assertTrue(e.getMessage().contains("Unknown definition /definitions/Blablabla"));
        }

        @Test
        void should_support_custom_JsonNode() throws IOException, ProcessingException {
            // Given
            InputStream spec = getClass().getResourceAsStream("/jsonNode/spec-JsonNode.json");
            JsonNode schema = Json.mapper().readTree(spec);

            HashMap<String, String> transformations = new HashMap<>();
            transformations.put("x-oneof", "x-oneOf");

            SwaggerValidator validator = SwaggerValidator.forJsonNode(schema, transformations);
            JsonNode sample = Json.mapper().readTree("{}");

            // When
            final ProcessingReport report = validator.validate(sample, "/definitions/User", true);

            // Then
            assertFalse(report.isSuccess());
            assertTrue(report.toString().contains("instance failed to match exactly one schema (matched 0 out of 2)"));
        }

        @Test
        void should_support_references_to_definitions_in_same_file() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/references/spec-issue14.json");
            JsonNode sample = buildSample("/references/sample-issue14.json");

            // When
            final ProcessingReport report = validator.validate(sample, "/definitions/Response");

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
        }

        @Test
        void should_support_forbidden_additional_properties() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/issue13/spec-issue13.json");
            JsonNode sample = Json.mapper().readTree("{\"hell\":\"World\",\"there\":\"Hi\"}");

            // When
            final ProcessingReport report = validator.validate(sample, "/definitions/ResponseBean", true);

            // Then
            assertFalse(report.isSuccess());
            List<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(
                "object instance has properties which are not allowed by the schema: [\"hell\"]",
                messages.get(0).getMessage()
            );
            assertEquals(
                "object has missing required properties ([\"hello\"])",
                messages.get(1).getMessage()
            );
        }
    }

    @Nested
    class LibraryCustomizations {
        @Test
        void should_accept_example_and_xml() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/keywords.json");
            JsonNode sample = Json.mapper().readTree("{\"name\": \"\"}");

            // When
            final ProcessingReport report = validator.validate(sample, "/definitions/User", true);

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
        }

        @Test
        void should_support_int32_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"int32\": 9999}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
        }

        @Test
        void should_validate_int32_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"int32\": 99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(1, messages.size());

            ProcessingMessage message = messages.get(0);
            assertEquals(LogLevel.WARNING, message.getLogLevel());
            assertTrue(message.getMessage().contains("value for int32 leads to overflow (found: 9999"));
        }

        @Test
        void should_support_int64_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"int64\": 9999}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
        }

        @Test
        void should_validate_int64_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"int64\": 99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(1, messages.size());

            ProcessingMessage message = messages.get(0);
            assertEquals(LogLevel.WARNING, message.getLogLevel());
            assertTrue(message.getMessage().contains("value for int64 leads to overflow (found: 9999"));
        }

        @Test
        void should_support_float_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"float\": 100.99}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
        }

        @Test
        void should_validate_float_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"float\": 1519348261000.0}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(1, messages.size());

            ProcessingMessage processingMessage = messages.get(0);
            assertEquals(LogLevel.WARNING, processingMessage.getLogLevel());
            assertEquals(
                "value for float leads to overflow (original: 1.519348261E+12, converted: 1.51934822E12)",
                processingMessage.getMessage()
            );
        }

        @Test
        void should_support_double_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            String sample = "{\"double\": 100.99}";

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
        }

        @Test
        void should_validate_double_format() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/swagger/formats.json");
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.set("double", new DecimalNode(new BigDecimal("0.343597383683435973836834359738368343597383683435973836834359738368")));

            // When
            ProcessingReport report = validator.validate(node, "/definitions/User");

            // Then
            assertTrue(report.isSuccess());
            ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(1, messages.size());

            ProcessingMessage processingMessage = messages.get(0);
            assertEquals(LogLevel.WARNING, processingMessage.getLogLevel());
            assertTrue(processingMessage.getMessage().contains("value for double leads to overflow"));

        }
    }

    private void assertNoWarnings(ProcessingReport report) {
        List<ProcessingMessage> messages = ImmutableList.copyOf(report);
        List<ProcessingMessage> warnings = messages.stream()
            .filter(msg -> msg.getLogLevel() == LogLevel.WARNING)
            .collect(Collectors.toList());

        assertEquals(0, warnings.size(), () -> "Unexpected warnings: " + report.toString());
    }

    @Nested
    class DeepCheck {
        @Test
        void should_accept_valid_document_when_deepCheck_is_true() throws IOException, ProcessingException {
            // Given
            SwaggerValidator validator = buildValidator("/deepCheck/spec.yaml");
            JsonNode sample = buildSample("/deepCheck/valid.json");

            // When
            ProcessingReport report = validator.validate(sample, "/definitions/User", true);

            // Then
            assertTrue(report.isSuccess());
            assertNoWarnings(report);
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
    }

    @Nested
    class Transformations {
        @Test
        void should_support_oneOf() throws IOException, ProcessingException {
            SwaggerValidator validator = buildValidator("/oneOf/spec.yaml");

            InputStreamReader sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/valid.json"));
            ProcessingReport report = validator.validate(CharStreams.toString(sample), "/definitions/User");
            assertTrue(report.isSuccess());
            ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(0, messages.size());

            sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/valid2.json"));
            report = validator.validate(CharStreams.toString(sample), "/definitions/User");
            assertTrue(report.isSuccess());
            assertNoWarnings(report);

            sample = new InputStreamReader(getClass().getResourceAsStream("/oneOf/invalid.json"));
            report = validator.validate(CharStreams.toString(sample), "/definitions/User");
            assertFalse(report.isSuccess());
            messages = ImmutableList.copyOf(report);
            assertEquals(1, messages.size());
            assertEquals("instance failed to match exactly one schema (matched 2 out of 2)", messages.get(0).getMessage());
        }

        @Test
        void should_support_allOf() throws IOException, ProcessingException {
            SwaggerValidator validator = buildValidator("/allOf/spec.yaml");

            InputStreamReader sample = new InputStreamReader(getClass().getResourceAsStream("/allOf/valid.json"));
            ProcessingReport report = validator.validate(CharStreams.toString(sample), "/definitions/Dog");
            assertTrue(report.isSuccess());
            ImmutableList<ProcessingMessage> messages = ImmutableList.copyOf(report);
            assertEquals(0, messages.size());

            sample = new InputStreamReader(getClass().getResourceAsStream("/allOf/invalid.json"));
            report = validator.validate(CharStreams.toString(sample), "/definitions/Dog");
            assertFalse(report.isSuccess());
            messages = ImmutableList.copyOf(report);
            assertEquals(1, messages.size());
            assertEquals("instance failed to match all required schemas (matched only 1 out of 2)", messages.get(0).getMessage());
        }

        @Test
        void should_transform_correctly() throws IOException {
            InputStream spec = getClass().getResourceAsStream("/transformations/spec-before.json");
            SwaggerValidator validator = SwaggerValidator.forJsonSchema(new InputStreamReader(spec));

            JsonNode schemaObject = (JsonNode) readFieldValue(SwaggerValidator.class, "schemaObject", validator)
                .orElse(null);
            assertNotNull(schemaObject);

            assertEquals(Json.mapper().readTree(getClass().getResourceAsStream("/transformations/spec-after.json")),
                schemaObject);
        }

        @Test
        void should_transform_nested_properties() throws IOException, ProcessingException {
            // Given
            InputStream spec = getClass().getResourceAsStream("/nested/spec-nested.json");
            SwaggerValidator validator = SwaggerValidator.forJsonSchema(new InputStreamReader(spec));

            // When
            final ProcessingReport report = validator.validate("{\"someProperty\": [{}]}", "/definitions/MyModel");

            // Then
            assertFalse(report.isSuccess());
            assertTrue(report.toString()
                .contains("error: instance failed to match exactly one schema (matched 0 out of 2)"));
        }
    }

    private SwaggerValidator buildValidator(String pathToSpec) throws IOException {
        InputStream spec = getClass().getResourceAsStream(pathToSpec);
        return SwaggerValidator.forYamlSchema(new InputStreamReader(spec));
    }

    private JsonNode buildSample(String s) throws IOException {
        return Json.mapper().readTree(new InputStreamReader(getClass().getResourceAsStream(s)));
    }
}
