/*
 * Copyright @ 2016 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.bjansen.ssv;

import com.github.bjansen.ssv.formats.*;
import com.github.bjansen.ssv.keywords.ExampleKeyword;
import com.github.bjansen.ssv.keywords.XmlKeyword;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.messages.JsonSchemaSyntaxMessageBundle;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.format.draftv3.DateAttribute;
import com.github.fge.jsonschema.library.DraftV4Library;
import com.github.fge.jsonschema.library.Keyword;
import com.github.fge.jsonschema.library.KeywordBuilder;
import com.github.fge.jsonschema.library.Library;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.messages.JsonSchemaValidationBundle;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.bundle.PropertiesBundle;
import com.github.fge.msgsimple.load.MessageBundleLoader;
import com.github.fge.msgsimple.load.MessageBundles;

import static com.github.fge.msgsimple.load.MessageBundles.getBundle;

/**
 * Library that extends the JSON Schema v4 and adds the additional keywords introduced by the
 * OpenAPI / Swagger v2.0 specification.
 */
class SwaggerV20Library {

    private SwaggerV20Library() { }

    private static final String OAI_V2_METASCHEMA_URI = "https://openapis.org/specification/versions/2.0#";

    private static Library get() {
        // The discriminator validator holds state that may persist in the event of a runtime exception etc.
        // Re-create the library to ensure this state doesn't persist between validations.
        return DraftV4Library.get().thaw()
            .addFormatAttribute("int32", Int32Attribute.getInstance())
            .addFormatAttribute("int64", Int64Attribute.getInstance())
            .addFormatAttribute("float", FloatAttribute.getInstance())
            .addFormatAttribute("double", DoubleAttribute.getInstance())
            .addFormatAttribute("date", DateAttribute.getInstance())
            .addFormatAttribute("byte", Base64Attribute.getInstance())

            .addKeyword(ExampleKeyword.getInstance())
            .addKeyword(XmlKeyword.getInstance())

            .freeze();
    }

    /**
     * @param logLevel log level
     * @param exceptionThreshold exception threshold
     * @return A {@link JsonSchemaFactory} instance configured with the OpenAPI / Swagger V20 metaschema library suitable
     * for use in validating OpenAPI / Swagger documents
     */
    static JsonSchemaFactory schemaFactory(final LogLevel logLevel, final LogLevel exceptionThreshold) {
        return JsonSchemaFactory
            .newBuilder()
            .setValidationConfiguration(
                ValidationConfiguration.newBuilder()
                    .setDefaultLibrary(OAI_V2_METASCHEMA_URI, SwaggerV20Library.get())
                    .setSyntaxMessages(getBundle(SwaggerV20Library.SyntaxBundle.class))
                    .setValidationMessages(getBundle(SwaggerV20Library.ValidationBundle.class))
                    .freeze())
            .setReportProvider(
                // Only emit ERROR and above from the JSON schema validation
                new ListReportProvider(logLevel, exceptionThreshold))
            .freeze();
    }

    /**
     * Message bundle loader that appends messages for the Swagger V20 extensions to the standard
     * JSON Schema syntax bundle.
     */
    public static class SyntaxBundle implements MessageBundleLoader {

        private static final String PATH = "/swagger/validation/schema-validation.properties";

        @Override
        public MessageBundle getBundle() {
            return MessageBundles
                .getBundle(JsonSchemaSyntaxMessageBundle.class)
                .thaw()
                .appendBundle(PropertiesBundle.forPath(PATH))
                .freeze();
        }
    }

    /**
     * Message bundle loader that appends messages for the Swagger V20 extensions to the standard
     * JSON Schema validation message bundle.
     */
    public static class ValidationBundle implements MessageBundleLoader {

        private static final String PATH = "/swagger/validation/schema-validation.properties";

        @Override
        public MessageBundle getBundle() {
            return MessageBundles
                .getBundle(JsonSchemaValidationBundle.class)
                .thaw()
                .appendBundle(PropertiesBundle.forPath(PATH))
                .freeze();
        }
    }
}
