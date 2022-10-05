package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum GuidingPrinciples implements Rule {

    FOR_HUMANS {
        @Override
        public Failure validate(Node node) {
            return node instanceof Document ? validateForHumans((Document) node) : null;
        }
    },
    ENTRY_FOR_EVERY_VERSIONS {
        @Override
        public Failure validate(Node node) {
            return node instanceof Heading ? validateEntryForEveryVersions((Heading) node) : null;
        }
    },
    TYPE_OF_CHANGES_GROUPED {
        @Override
        public Failure validate(Node node) {
            return node instanceof Heading ? validateTypeOfChangesGrouped((Heading) node) : null;
        }
    },
    LINKABLE {
        @Override
        public Failure validate(Node node) {
            return node instanceof Heading ? validateLinkable((Heading) node) : null;
        }
    },
    LATEST_VERSION_FIRST {
        @Override
        public Failure validate(Node node) {
            return node instanceof Document ? validateLatestVersionFirst((Document) node) : null;
        }
    },
    DATE_DISPLAYED {
        @Override
        public Failure validate(Node node) {
            return null;
        }
    },
    SEMVER {
        @Override
        public Failure validate(Node node) {
            return null;
        }
    };

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @VisibleForTesting
    static Failure validateForHumans(@NotNull Document document) {
        List<Heading> headings = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Changelog::isChangelogLevel)
                .collect(Collectors.toList());

        switch (headings.size()) {
            case 0:
                return Failure.of(FOR_HUMANS, "Missing Changelog heading", document);
            case 1:
                try {
                    Changelog.parse(headings.get(0));
                    return null;
                } catch (IllegalArgumentException ex) {
                    return Failure.of(FOR_HUMANS, ex.getMessage(), document);
                }
            default:
                return Failure.of(FOR_HUMANS, "Too many Changelog headings", document);
        }
    }

    @VisibleForTesting
    static Failure validateEntryForEveryVersions(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return null;
        }
        try {
            Version.parse(heading);
        } catch (IllegalArgumentException ex) {
            return Failure.of(ENTRY_FOR_EVERY_VERSIONS, ex.getMessage(), heading);
        }
        return null;
    }

    @VisibleForTesting
    static Failure validateTypeOfChangesGrouped(@NotNull Heading heading) {
        if (!TypeOfChange.isTypeOfChangeLevel(heading)) {
            return null;
        }
        try {
            TypeOfChange.parse(heading);
        } catch (IllegalArgumentException ex) {
            return Failure.of(TYPE_OF_CHANGES_GROUPED, ex.getMessage(), heading);
        }
        return null;
    }

    @VisibleForTesting
    static Failure validateLinkable(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return null;
        }

        try {
            Version version = Version.parse(heading);

            ReferenceRepository repository = Parser.REFERENCES.get(heading.getDocument());
            String normalizeRef = repository.normalizeKey(version.getRef());
            Reference reference = repository.get(normalizeRef);

            return reference == null
                    ? Failure.of(LINKABLE, "Missing reference '" + version.getRef() + "'", heading)
                    : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @VisibleForTesting
    static Failure validateLatestVersionFirst(@NotNull Document doc) {
        List<VersionNode> versions = VersionNode.allOf(doc);

        Comparator<VersionNode> comparator = Comparator.comparing((VersionNode item) -> item.getVersion().getDate()).reversed();
        VersionNode unsortedItem = getFirstUnsortedItem(versions, comparator);
        return unsortedItem != null ? Failure.of(LATEST_VERSION_FIRST, "Versions not sorted", unsortedItem.getNode()) : null;
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

    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public Stream<Rule> getProviders() {
            return Stream.of(GuidingPrinciples.values());
        }
    }
}
