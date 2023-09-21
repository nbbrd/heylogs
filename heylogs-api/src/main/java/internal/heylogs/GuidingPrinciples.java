package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nbbrd.heylogs.Util.illegalArgumentToNull;

public enum GuidingPrinciples implements Rule {

    FOR_HUMANS {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof Document ? validateForHumans((Document) node) : NO_PROBLEM;
        }
    },
    ALL_H2_CONTAIN_A_VERSION {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof Heading ? validateAllH2ContainAVersion((Heading) node) : NO_PROBLEM;
        }
    },
    TYPE_OF_CHANGES_GROUPED {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof Heading ? validateTypeOfChangesGrouped((Heading) node) : NO_PROBLEM;
        }
    },
    LINKABLE {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof Heading ? validateLinkable((Heading) node) : NO_PROBLEM;
        }
    },
    LATEST_VERSION_FIRST {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof Document ? validateLatestVersionFirst((Document) node) : NO_PROBLEM;
        }
    },
    DATE_DISPLAYED {
        @Override
        public Failure validate(@NotNull Node node) {
            return NO_PROBLEM;
        }
    };

    @Override
    public @NotNull String getId() {
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
                return Failure
                        .builder()
                        .rule(FOR_HUMANS)
                        .message("Missing Changelog heading")
                        .location(document)
                        .build();
            case 1:
                try {
                    Changelog.parse(headings.get(0));
                    return NO_PROBLEM;
                } catch (IllegalArgumentException ex) {
                    return Failure
                            .builder()
                            .rule(FOR_HUMANS)
                            .message(ex.getMessage())
                            .location(document)
                            .build();
                }
            default:
                return Failure
                        .builder()
                        .rule(FOR_HUMANS)
                        .message("Too many Changelog headings")
                        .location(document)
                        .build();
        }
    }

    @VisibleForTesting
    static Failure validateAllH2ContainAVersion(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return NO_PROBLEM;
        }
        try {
            Version.parse(heading);
        } catch (IllegalArgumentException ex) {
            return Failure
                    .builder()
                    .rule(ALL_H2_CONTAIN_A_VERSION)
                    .message(ex.getMessage())
                    .location(heading)
                    .build();
        }
        return NO_PROBLEM;
    }

    @VisibleForTesting
    static Failure validateTypeOfChangesGrouped(@NotNull Heading heading) {
        if (!TypeOfChange.isTypeOfChangeLevel(heading)) {
            return NO_PROBLEM;
        }
        try {
            TypeOfChange.parse(heading);
        } catch (IllegalArgumentException ex) {
            return Failure
                    .builder()
                    .rule(TYPE_OF_CHANGES_GROUPED)
                    .message(ex.getMessage())
                    .location(heading)
                    .build();
        }
        return NO_PROBLEM;
    }

    @VisibleForTesting
    static Failure validateLinkable(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return NO_PROBLEM;
        }

        try {
            Version version = Version.parse(heading);

            ReferenceRepository repository = Parser.REFERENCES.get(heading.getDocument());
            String normalizeRef = repository.normalizeKey(version.getRef());
            Reference reference = repository.get(normalizeRef);

            return reference == null
                    ? Failure
                    .builder()
                    .rule(LINKABLE)
                    .message("Missing reference '" + version.getRef() + "'")
                    .location(heading)
                    .build()
                    : NO_PROBLEM;
        } catch (IllegalArgumentException ex) {
            return NO_PROBLEM;
        }
    }

    @VisibleForTesting
    static Failure validateLatestVersionFirst(@NotNull Document doc) {
        List<VersionNode> versions = VersionNode.allOf(doc);

        Comparator<VersionNode> comparator = Comparator.comparing((VersionNode item) -> item.getVersion().getDate()).reversed();
        VersionNode unsortedItem = getFirstUnsortedItem(versions, comparator);
        return unsortedItem != null
                ? Failure
                .builder()
                .rule(LATEST_VERSION_FIRST)
                .message("Versions not sorted")
                .location(unsortedItem.getNode())
                .build()
                : NO_PROBLEM;
    }

    @lombok.Value
    private static class VersionNode {
        Version version;
        Node node;

        static VersionNode parse(Heading node) {
            return new VersionNode(Version.parse(node), node);
        }

        static List<VersionNode> allOf(Document doc) {
            return Nodes.of(Heading.class)
                    .descendants(doc)
                    .filter(Version::isVersionLevel)
                    .map(illegalArgumentToNull(VersionNode::parse))
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

    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public Stream<Rule> getProviders() {
            return Stream.of(GuidingPrinciples.values());
        }
    }
}
