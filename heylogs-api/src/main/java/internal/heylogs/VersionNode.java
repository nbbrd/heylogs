package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.util.ast.Document;
import lombok.NonNull;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Version;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@lombok.Value
public class VersionNode {

    @NonNull
    Heading heading;

    @NonNull
    Version version;

    @NonNull
    Reference reference;

    public static VersionNode of(Version version, URL url) {
        return new VersionNode(
                version.toHeading(), version,
                ChangelogNodes.newReference(version, url));
    }

    public static List<VersionNode> allOf(Document doc, ReferenceRepository repository) {
        return Nodes.of(Heading.class).descendants(doc)
                .filter(Version::isVersionLevel)
                .map(heading -> {
                    Version version = Version.parse(heading);
                    return new VersionNode(heading, version, repository.getFromRaw(version.getRef()));
                })
                .collect(toList());
    }

    public static Optional<VersionNode> getUnreleased(List<VersionNode> list) {
        return list.stream()
                .filter(versionNode -> versionNode.getVersion().isUnreleased())
                .findFirst();
    }

    public static Optional<VersionNode> getLatest(List<VersionNode> list) {
        return list.stream()
                .filter(versionNode -> !versionNode.getVersion().isUnreleased())
                .max(comparing(item -> item.getVersion().getDate()));
    }

    public URL getURL() {
        return URLExtractor.urlOf(getReference().getUrl());
    }
}
