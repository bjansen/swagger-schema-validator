package com.github.bjansen.ssv.keywords;

import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonschema.core.keyword.syntax.checkers.AbstractSyntaxChecker;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import com.github.fge.jsonschema.library.Keyword;
import com.github.fge.msgsimple.bundle.MessageBundle;
import java.util.Collection;

/**
 * A {@link Keyword} for the {@code example} property, used to include an example of an instance for its parent schema.
 */
public class ExampleKeyword {

    public static Keyword getInstance() {
        return Keyword.newBuilder("example")
            .withSyntaxChecker(ExampleSyntaxChecker.INSTANCE)
            .freeze();
    }

    private static class ExampleSyntaxChecker extends AbstractSyntaxChecker {

        private static final ExampleSyntaxChecker INSTANCE = new ExampleSyntaxChecker();

        private ExampleSyntaxChecker() {
            super("example", NodeType.OBJECT);
        }

        @Override
        protected void checkValue(Collection<JsonPointer> collection,
            MessageBundle messageBundle,
            ProcessingReport processingReport,
            SchemaTree schemaTree) {

        }
    }
}

