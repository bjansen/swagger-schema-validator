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
 * A {@link Keyword} for the {@code discriminator} property, used for polymorphism.
 */
public class DiscriminatorKeyword {

    private DiscriminatorKeyword() {
        throw new IllegalStateException();
    }

    public static Keyword getInstance() {
        return Keyword.newBuilder("discriminator")
            .withSyntaxChecker(SyntaxChecker.INSTANCE)
            .freeze();
    }

    private static class SyntaxChecker extends AbstractSyntaxChecker {

        private static final SyntaxChecker INSTANCE = new SyntaxChecker();

        private SyntaxChecker() {
            super("discriminator", NodeType.STRING);
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

