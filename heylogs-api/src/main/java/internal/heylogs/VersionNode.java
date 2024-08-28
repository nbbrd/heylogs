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
import java.util.Objects;
import java.util.Optional;

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

    public static List<VersionNode> allOf(Document document, ReferenceRepository repository) {
        return Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(heading -> {
                    try {
                        Version version = Version.parse(heading);
                        return new VersionNode(heading, version, Objects.requireNonNull(repository.getFromRaw(version.getRef())));
                    } catch (RuntimeException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public static Optional<VersionNode> findUnreleased(List<VersionNode> list) {
        return list.stream()
                .filter(versionNode -> versionNode.getVersion().isUnreleased())
                .findFirst();
    }

    public URL getURL() {
        return URLExtractor.urlOf(getReference().getUrl());
    }
}
