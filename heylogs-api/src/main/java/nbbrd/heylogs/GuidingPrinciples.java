package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static nbbrd.heylogs.Rule.invalidNode;

public enum GuidingPrinciples implements Rule<Node> {

    FOR_HUMANS {
        @Override
        public String validate(Node node) {
            return node instanceof Heading ? validateForHumans((Heading) node) : null;
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

    private static String validateForHumans(@NotNull Heading heading) {
        if (!Changelog.isChangelogLevel(heading)) {
            return null;
        }
        try {
            Changelog.parse(heading);
        } catch (IllegalArgumentException ex) {
            return invalidNode(heading, ex.getMessage());
        }
        return null;
    }

    private static String validateEntryForEveryVersions(@NotNull Heading heading) {
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

    private static String validateTypeOfChangesGrouped(@NotNull Heading heading) {
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

    private static String validateLinkable(@NotNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return null;
        }
        try {
            return !Version.parse(heading).isLink() ? invalidNode(heading, "Not linkable") : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @lombok.Value
    private static class VersionNode {
        Version version;
        Node node;
    }

    static String validateLatestVersionFirst(@NotNull Document parent) {
        List<VersionNode> versions = Nodes.of(Heading.class)
                .descendants(parent)
                .filter(Version::isVersionLevel)
                .map(node -> new VersionNode(Version.parse(node), node))
                .collect(Collectors.toList());

        Comparator<VersionNode> comparator = Comparator.comparing((VersionNode item) -> item.getVersion().getDate()).reversed();
        VersionNode unsortedItem = getFirstUnsortedItem(versions, comparator);
        return unsortedItem != null ? invalidNode(unsortedItem.getNode(), "Versions not sorted") : null;
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
