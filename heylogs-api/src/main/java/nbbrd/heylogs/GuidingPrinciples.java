package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static nbbrd.heylogs.Rule.invalidNode;

public enum GuidingPrinciples implements Rule<Node> {

    FOR_HUMANS {
        @Override
        public String validate(Node node) {
            return node instanceof Document ? validateForHumans((Document) node) : null;
        }
    },
    ENTRY_FOR_EVERY_VERSIONS {
        @Override
        public String validate(Node node) {
            return node instanceof Heading ? validateEntryForEveryVersions((Heading) node) : null;
        }
    },
    TYPE_OF_CHANGES_GROUPED {
        @Override
        public String validate(Node node) {
            return node instanceof Heading ? validateTypeOfChangesGrouped((Heading) node) : null;
        }
    },
    LINKABLE {
        @Override
        public String validate(Node node) {
            return node instanceof Heading ? validateLinkable((Heading) node) : null;
        }
    },
    LATEST_VERSION_FIRST {
        @Override
        public String validate(Node node) {
            return node instanceof Document ? validateLatestVersionFirst((Document) node) : null;
        }
    },
    DATE_DISPLAYED {
        @Override
        public String validate(Node node) {
            return null;
        }
    },
    SEMVER {
        @Override
        public String validate(Node node) {
            return null;
        }
    };

    @VisibleForTesting
    static String validateForHumans(@NotNull Document document) {
        List<Heading> headings = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Changelog::isChangelogLevel)
                .collect(Collectors.toList());

        switch (headings.size()) {
            case 0:
                return invalidNode(document, "Missing Changelog heading");
            case 1:
                try {
                    Changelog.parse(headings.get(0));
                    return null;
                } catch (IllegalArgumentException ex) {
                    return invalidNode(document, ex.getMessage());
                }
            default:
                return invalidNode(document, "Too many Changelog headings");
        }
    }

    @VisibleForTesting
    static String validateEntryForEveryVersions(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return null;
        }
        try {
            Version.parse(heading);
        } catch (IllegalArgumentException ex) {
            return invalidNode(heading, ex.getMessage());
        }
        return null;
    }

    @VisibleForTesting
    static String validateTypeOfChangesGrouped(@NotNull Heading heading) {
        if (!TypeOfChange.isTypeOfChangeLevel(heading)) {
            return null;
        }
        try {
            TypeOfChange.parse(heading);
        } catch (IllegalArgumentException ex) {
            return invalidNode(heading, ex.getMessage());
        }
        return null;
    }

    @VisibleForTesting
    static String validateLinkable(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return null;
        }

        Version version = Version.parse(heading);

        ReferenceRepository repository = Parser.REFERENCES.get(heading.getDocument());
        String normalizeRef = repository.normalizeKey(version.getRef());
        Reference reference = repository.get(normalizeRef);

        return reference == null
                ? invalidNode(heading, "Missing reference '" + version.getRef() + "'")
                : null;
    }

    @VisibleForTesting
    static String validateLatestVersionFirst(@NotNull Document doc) {
        List<VersionNode> versions = VersionNode.allOf(doc);

        Comparator<VersionNode> comparator = Comparator.comparing((VersionNode item) -> item.getVersion().getDate()).reversed();
        VersionNode unsortedItem = getFirstUnsortedItem(versions, comparator);
        return unsortedItem != null ? invalidNode(unsortedItem.getNode(), "Versions not sorted") : null;
    }

    @lombok.Value
    private static class VersionNode {
        Version version;
        Node node;

        static List<VersionNode> allOf(Document doc) {
            return Nodes.of(Heading.class)
                    .descendants(doc)
                    .filter(Version::isVersionLevel)
                    .map(node -> {
                        try {
                            return new VersionNode(Version.parse(node), node);
                        } catch (IllegalArgumentException ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

    }

    private static <T> T getFirstUnsortedItem(List<T> list, Comparator<T> comparator) {
        if ((list.isEmpty()) || list.size() == 1) {
            return null;
        }

        Iterator<T> iterator = list.iterator();
        T current, previous = iterator.next();
        while (iterator.hasNext()) {
            current = iterator.next();
            if (comparator.compare(previous, current) > 0) {
                return current;
            }
            previous = current;
        }
        return null;
    }
}
