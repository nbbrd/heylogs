package internal.heylogs;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.TypeOfChange;
import nbbrd.heylogs.Version;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static java.util.stream.Collectors.toList;

public final class ChangelogNodes {

    private ChangelogNodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isNotVersionHeading(Node node) {
        return !(node instanceof Heading && Version.isVersionLevel((Heading) node));
    }

    public static boolean isTypeOfChangeNode(Node node) {
        return node instanceof Heading && TypeOfChange.isTypeOfChangeLevel((Heading) node);
    }

    public static boolean isNotHeading(Node next) {
        return !(next instanceof Heading);
    }

    public static boolean isUnreleasedHeading(Heading heading) {
        try {
            return Version.isVersionLevel(heading) && Version.parse(heading).isUnreleased();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static Optional<Heading> getUnreleasedHeading(Node doc) {
        return Nodes.of(Heading.class)
                .descendants(doc)
                .filter(ChangelogNodes::isUnreleasedHeading)
                .findFirst();
    }

    public static Map<TypeOfChange, List<BulletListItem>> getBulletListsByTypeOfChange(Heading version) {
        TreeMap<TypeOfChange, List<BulletListItem>> result = new TreeMap<>();
        Nodes.next(version, ChangelogNodes::isNotVersionHeading)
                .filter(ChangelogNodes::isTypeOfChangeNode)
                .map(Heading.class::cast)
                .forEach(typeOfChange -> result.put(TypeOfChange.parse(typeOfChange), collect(typeOfChange)));
        return result;
    }

    private static List<BulletListItem> collect(Heading typeOfChange) {
        return Nodes.next(typeOfChange, ChangelogNodes::isNotHeading)
                .filter(BulletList.class::isInstance)
                .map(BulletList.class::cast)
                .flatMap(Nodes.of(BulletListItem.class)::descendants)
                .collect(toList());
    }
}
