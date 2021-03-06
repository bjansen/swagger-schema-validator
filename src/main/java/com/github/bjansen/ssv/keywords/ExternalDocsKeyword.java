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
 * A {@link Keyword} for the {@code externalDocs} property, used to provide additional docs for a schema.
 */
public class ExternalDocsKeyword {

    private ExternalDocsKeyword() {
        throw new IllegalStateException();
    }

    public static Keyword getInstance() {
        return Keyword.newBuilder("externalDocs")
            .withSyntaxChecker(SyntaxChecker.INSTANCE)
            .freeze();
    }

    private static class SyntaxChecker extends AbstractSyntaxChecker {

        private static final SyntaxChecker INSTANCE = new SyntaxChecker();

        private SyntaxChecker() {
            super("externalDocs", NodeType.OBJECT);
        }

        @Override
        protected void checkValue(Collection<JsonPointer> collection,
            MessageBundle messageBundle,
            ProcessingReport processingReport,
            SchemaTree schemaTree) {

            // nothing to do
        }
    }
}

