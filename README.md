# Swagger schema validator

This library validates JSON objects against models defined in the `declarations` section of a Swagger 2 specification.

```java
InputStream spec = getClass().getResourceAsStream("mySpec.yaml");
SwaggerValidator validator = SwaggerValidator.forYamlSchema(spec);

ProcessingReport report = validator.validate("{\"name\": \"Bob\"}", "/definitions/User");

if (report.isSuccess()) {
    doStuff();
}
```

## Additional schema validations

In addition to the subset of JSON-Schema Draft 4 already supported by Swagger 2 specification, this library
adds support for the following JSON-Schema validation keywords:

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
