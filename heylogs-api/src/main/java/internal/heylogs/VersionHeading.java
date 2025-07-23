package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import internal.heylogs.spi.URLExtractor;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Version;

import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

import static nbbrd.heylogs.Util.illegalArgumentToNull;

@lombok.Value(staticConstructor = "of")
public class VersionHeading implements SectionHeading<Version> {

    public static boolean isParsable(@NonNull Node node) {
        return node instanceof Heading && Version.isVersionLevel((Heading) node);
    }

    @StaticFactoryMethod
    public static @NonNull VersionHeading of(@NonNull Version version, @NonNull URL url) {
        return new VersionHeading(
                version.toHeading(), version,
                newReference(version, url));
    }

    @StaticFactoryMethod
    public static @NonNull VersionHeading parse(@NonNull ReferenceRepository repository, @NonNull Node node) {
        if (!(node instanceof Heading)) {
            throw new IllegalArgumentException("Node must be an instance of Heading");
        }
        Version version = Version.parse((Heading) node);
        Reference ref = repository.getFromRaw(version.getRef());
        if (ref == null) {
            throw new IllegalArgumentException("Missing reference");
        }
        return new VersionHeading((Heading) node, version, ref);
    }

    @NonNull
    Heading heading;

    @NonNull
    Version section;

    @NonNull
    Reference reference;

    public URL getURL() {
        return URLExtractor.urlOf(getReference().getUrl());
    }

    public @NonNull Stream<TypeOfChangeHeading> getTypeOfChanges() {
        return Nodes
                .nextWhile(heading, node -> !isParsable(node))
                .filter(TypeOfChangeHeading::isParsable)
                .map(illegalArgumentToNull(TypeOfChangeHeading::parse))
                .filter(Objects::nonNull);
    }

    public static Reference newReference(Version newVersion, URL newURL) {
        return new Reference(BasedSequence.of("[" + newVersion.getRef() + "]: " + newURL), null, null);
    }
}
