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
 * A {@link Keyword} for the {@code readOnly} property, used to declare a property as read-only.
 */
public class ReadOnlyKeyword {

    public static Keyword getInstance() {
        return Keyword.newBuilder("readOnly")
            .withSyntaxChecker(SyntaxChecker.INSTANCE)
            .freeze();
    }

    private static class SyntaxChecker extends AbstractSyntaxChecker {

        private static final SyntaxChecker INSTANCE = new SyntaxChecker();

        private SyntaxChecker() {
            super("readOnly", NodeType.BOOLEAN);
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

