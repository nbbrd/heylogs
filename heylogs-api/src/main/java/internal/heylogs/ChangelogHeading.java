package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.Changelog;
import nbbrd.heylogs.Nodes;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static nbbrd.heylogs.Util.illegalArgumentToNull;

@lombok.Value(staticConstructor = "of")
public class ChangelogHeading implements SectionHeading<Changelog> {

    public static @NonNull Optional<ChangelogHeading> root(@NonNull Document document) {
        return Nodes.of(Heading.class)
                .descendants(document)
                .filter(ChangelogHeading::isParsable)
                .map(ChangelogHeading::parse)
                .findFirst();
    }

    public static boolean isParsable(@NonNull Node node) {
        return node instanceof Heading && Changelog.isChangelogLevel((Heading) node);
    }

    @StaticFactoryMethod
    public static @NonNull ChangelogHeading parse(@NonNull Node node) {
        if (!(node instanceof Heading)) {
            throw new IllegalArgumentException("Node must be an instance of Heading");
        }
        return ChangelogHeading.of(Changelog.parse((Heading) node), (Heading) node, Parser.REFERENCES.get(node.getDocument()));
    }

    @NonNull
    Changelog section;

    @NonNull
    Heading heading;

    @NonNull
    ReferenceRepository repository;

    public @NonNull Stream<VersionHeading> getVersions() {
        return Nodes
                .nextWhile(heading, ChangelogHeading::isNotEndOfChangelog)
                .filter(VersionHeading::isParsable)
                .map(illegalArgumentToNull(node -> VersionHeading.parse(repository, node)))
                .filter(Objects::nonNull);
    }

    private static boolean isNotEndOfChangelog(Node ignore) {
        return true; // TODO: Implement logic to determine if parsing should continue
    }
}
