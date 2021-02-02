# Swagger schema validator ![](https://img.shields.io/maven-central/v/com.github.bjansen/swagger-schema-validator.svg?style=flat) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=swagger-schema-validator&metric=alert_status)](https://sonarcloud.io/dashboard?id=swagger-schema-validator)

This library validates JSON objects against models defined in the `definitions` section of a Swagger 2 specification.

```java
InputStream spec = getClass().getResourceAsStream("mySpec.yaml");
SwaggerValidator validator = SwaggerValidator.forYamlSchema(spec);

ProcessingReport report = validator.validate("{\"name\": \"Bob\"}", "/definitions/User");

if (report.isSuccess()) {
    doStuff();
}
```

## Installation

This library is available on Maven Central:

```xml
<dependency>
    <groupId>com.github.bjansen</groupId>
    <artifactId>swagger-schema-validator</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Additional schema validations

In addition to the subset of [JSON-Schema Draft 4](https://tools.ietf.org/html/draft-zyp-json-schema-04) already 
supported by Swagger 2 specifications, this library adds support for the following JSON-Schema validation keywords:

| Keyword             | Description                                                                                           |
|---------------------|-------------------------------------------------------------------------------------------------------|
| `additionalItems`   | See [#rfc.section.6.4.2](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.4.2) |
| `contains`          | See [#rfc.section.6.4.6](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.4.6) |
| `patternProperties` | See [#rfc.section.6.5.5](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.5.5) |
| `dependencies`      | See [#rfc.section.6.5.7](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.5.7) |
| `propertyNames`     | See [#rfc.section.6.5.8](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.5.8) |
| `if`                | See [#rfc.section.6.6.1](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.6.1) |
| `then`              | See [#rfc.section.6.6.2](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.6.2) |
| `else`              | See [#rfc.section.6.6.3](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.6.3) |
| `allOf`             | See [#rfc.section.6.7.1](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.7.1) |
| `anyOf`             | See [#rfc.section.6.7.2](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.7.2) |
| `oneOf`             | See [#rfc.section.6.7.3](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.7.3) |
| `not`               | See [#rfc.section.6.7.4](http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.7.4) |

To use them in a Swagger 2 spec, simply prefix them with `x-`, like this:

```yaml
definitions:
  User:
    x-oneOf: [{required: ["id"]}, {required: ["name"]}]
    properties:
      id:
        type: integer
      name:
        type: string
```

In the example above, a `User` will be valid if it contains either an `id` or a `name`.

## Custom schemas and validation keywords

In case you need more than the default factory methods to parse JSON/YAML schemas, or need to do extra keywords
transformations (like automatically renaming `x-oneof` to `x-oneOf` in schemas you don't control), you can use
this factory method that was introduced in 1.0.0:

```java
JsonNode schema = Json.mapper().readTree(getClass().getResourceAsStream("schema.json"));
Map<String, String> transformations = Map.of("x-oneof", "x-oneOf");

SwaggerValidator validator = SwaggerValidator.forJsonNode(schema, transformations);
```

Custom transformations will be applied before built-in ones, so in this case it will go `x-oneof` -> `x-oneOf` -> `oneOf`.

## How it works

This library is a bridge between a Swagger schema parser provided by [swagger-core](https://github.com/swagger-api/swagger-core)
and a general-purpose JSON schema validator provided by [json-schema-validator](https://github.com/java-json-tools/json-schema-validator).

As a consequence, it is pretty much limited to the features these two libraries offer.
