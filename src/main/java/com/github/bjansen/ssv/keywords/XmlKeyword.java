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
 * A {@link Keyword} for the {@code xml} property, used to configure additional metadata to describe the XML
 * representation format of a property.
 */
public class XmlKeyword {

    public static Keyword getInstance() {
        return Keyword.newBuilder("xml")
            .withSyntaxChecker(XmlSyntaxChecker.INSTANCE)
            .freeze();
    }

    private static class XmlSyntaxChecker extends AbstractSyntaxChecker {

        private static final XmlSyntaxChecker INSTANCE = new XmlSyntaxChecker();

        private XmlSyntaxChecker() {
            super("xml", NodeType.OBJECT);
        }

        @Override
        protected void checkValue(Collection<JsonPointer> collection,
            MessageBundle messageBundle,
            ProcessingReport processingReport,
            SchemaTree schemaTree) {

        }
    }
}

