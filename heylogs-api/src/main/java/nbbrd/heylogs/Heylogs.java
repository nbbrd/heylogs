package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.RefNode;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.TypeOfChangeHeading;
import internal.heylogs.VersionHeading;
import internal.heylogs.base.GuidingPrinciples;
import internal.heylogs.spi.URLExtractor;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static internal.heylogs.spi.RuleSupport.problemStreamOf;
import static internal.heylogs.spi.VersioningSupport.versioningStreamOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.TimeRange.toTimeRange;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Heylogs {

    @StaticFactoryMethod
    public static @NonNull Heylogs ofServiceLoader() {
        return Heylogs
                .builder()
                .rules(RuleLoader.load())
                .formats(FormatLoader.load())
                .versionings(VersioningLoader.load())
                .forges(ForgeLoader.load())
                .build();
    }

    @NonNull
    @lombok.Singular
    List<Rule> rules;

    @NonNull
    @lombok.Singular
    List<Format> formats;

    @NonNull
    @lombok.Singular
    List<Versioning> versionings;

    @NonNull
    @lombok.Singular
    List<Forge> forges;

    public @NonNull List<Problem> checkFormat(@NonNull Document document) {
        return problemStreamOf(document, rules).collect(toList());
    }

    public void extractVersions(@NonNull Document document, @NonNull Filter filter) {
        if (isNotValidAgainstGuidingPrinciples(document)) {
            throw new IllegalArgumentException("Invalid changelog");
        }

        int found = 0;
        boolean keep = false;

        List<String> refNodes = new ArrayList<>();
        List<Reference> references = new ArrayList<>();

        for (Node current : document.getChildren()) {

            boolean versionHeading = current instanceof Heading
                    && Version.isVersionLevel((Heading) current);

            if (versionHeading) {
                if (found >= filter.getLimit() || !filter.contains(Version.parse((Heading) current))) {
                    keep = false;
                } else {
                    found++;
                    keep = true;
                }
            }

            if (keep) {
                Nodes.of(RefNode.class)
                        .descendants(current)
                        .map(node -> node.getReference().toString())
                        .forEach(refNodes::add);
                if (versionHeading && filter.isIgnoreContent()) {
                    keep = false;
                }
            } else {
                if (current instanceof Reference) {
                    references.add((Reference) current);
                } else {
                    current.unlink();
                }
            }
        }

        references
                .stream()
                .filter(reference -> !refNodes.contains(reference.getReference().toString()))
                .forEach(Node::unlink);
    }

    public @NonNull List<Resource> listResources() {
        return concat(
                rules.stream().map(Resource::of),
                formats.stream().map(Resource::of),
                versionings.stream().map(Resource::of),
                forges.stream().map(Resource::of)
        ).sorted(Resource.DEFAULT_COMPARATOR).collect(toList());
    }

    public void releaseChanges(@NonNull Document document, @NonNull Version newVersion,
                               @NonNull String versionTagPrefix, @Nullable String versioningId) throws IllegalArgumentException {
        if (versioningId != null) {
            Versioning versioning = getVersioningById(versioningId)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find versioning with id '" + versioningId + "'"));
            if (!versioning.isValidVersion(newVersion.getRef())) {
                throw new IllegalArgumentException("Invalid version '" + newVersion.getRef() + "' for versioning '" + versioningId + "'");
            }
        }

        if (isNotValidAgainstGuidingPrinciples(document)) {
            throw new IllegalArgumentException("Invalid changelog");
        }

        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate changelog header"));

        VersionHeading unreleased = changelog.getVersions()
                .filter(versionNode -> versionNode.getSection().isUnreleased())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate unreleased header"));

        Forge forge = findForge(unreleased)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine forge"));

        URL releaseURL = forge.deriveCompareLink(unreleased.getURL(), versionTagPrefix + newVersion.getRef());
        VersionHeading release = VersionHeading.of(newVersion, releaseURL);

        URL updatedURL = forge.deriveCompareLink(releaseURL, "HEAD");
        VersionHeading updated = VersionHeading.of(unreleased.getSection(), updatedURL);

        changelog.getRepository().putRawKey(release.getReference().getReference(), release.getReference());
        changelog.getRepository().putRawKey(updated.getReference().getReference(), updated.getReference());

        unreleased.getHeading().appendChild(release.getHeading());
        unreleased.getReference().insertAfter(release.getReference());
        unreleased.getReference().insertBefore(updated.getReference());
        unreleased.getReference().unlink();
    }

    public @NonNull Summary scanContent(@NonNull Document document) {
        if (isNotValidAgainstGuidingPrinciples(document)) {
            return Summary.INVALID;
        }

        ChangelogHeading changelog = ChangelogHeading.root(document).orElse(null);

        if (changelog == null) {
            return Summary.INVALID;
        }

        List<VersionHeading> versions = changelog.getVersions().collect(toList());

        if (versions.isEmpty()) {
            return Summary.EMPTY;
        }

        Map<Boolean, List<VersionHeading>> versionsByStatus = versions.stream().collect(partitioningBy(version -> version.getSection().isReleased()));

        List<Version> releases = versionsByStatus.get(true)
                .stream()
                .map(VersionHeading::getSection)
                .collect(toList());

        long unreleasedChanges = versionsByStatus.get(false)
                .stream()
                .findFirst()
                .map(version -> version.getTypeOfChanges().flatMap(TypeOfChangeHeading::getBulletListItems).count())
                .orElse(0L);

        VersionHeading first = versions.get(0);
        Forge forgeOrNull = findForge(first).orElse(null);

        return Summary
                .builder()
                .valid(true)
                .releaseCount(releases.size())
                .timeRange(releases.stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL))
                .compatibilities(versioningStreamOf(versionings, releases).map(Versioning::getVersioningName).collect(toList()))
                .unreleasedChanges((int) unreleasedChanges)
                .forgeName(forgeOrNull != null ? forgeOrNull.getForgeName() : null)
                .forgeURL(getBaseURL(forgeOrNull, first.getURL()))
                .build();
    }

    public void formatProblems(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        getFormatById(formatId)
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatProblems(appendable, list);
    }

    public void formatStatus(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        getFormatById(formatId)
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatStatus(appendable, list);
    }

    public void formatResources(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        getFormatById(formatId)
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatResources(appendable, list);
    }

    public @NonNull Optional<String> getFormatIdByFile(@NonNull Path file) {
        return getFormatByFile(file).map(Format::getFormatId);
    }

    private URL getBaseURL(Forge forgeOrNull, URL url) {
        return forgeOrNull != null ? forgeOrNull.getProjectURL(url) : URLExtractor.baseOf(url);
    }

    private Optional<Forge> findForge(VersionHeading node) {
        return forges.stream()
                .filter(forge -> forge.isCompareLink(node.getURL()))
                .findFirst();
    }

    private Optional<Format> getFormatById(String formatId) {
        return formats.stream()
                .filter(onId(formatId))
                .findFirst();
    }

    private Optional<Format> getFormatByFile(Path file) {
        return formats.stream()
                .filter(onFile(file))
                .findFirst();
    }

    private Optional<Versioning> getVersioningById(String versioningId) {
        return versionings.stream()
                .filter(versioning -> versioning.getVersioningId().equals(versioningId))
                .findFirst();
    }

    public static final String FIRST_FORMAT_AVAILABLE = "";

    private static boolean isNotValidAgainstGuidingPrinciples(Document document) {
        return problemStreamOf(document, asList(GuidingPrinciples.values())).findFirst().isPresent();
    }

    @MightBePromoted
    @SafeVarargs
    private static <T> Stream<T> concat(Stream<T> first, Stream<T>... rest) {
        Stream<T> result = first;
        for (Stream<T> next : rest) {
            result = Stream.concat(result, next);
        }
        return result;
    }

    private static Predicate<Format> onId(String id) {
        return format -> id.equals(FIRST_FORMAT_AVAILABLE) || format.getFormatId().equals(id);
    }

    private static Predicate<Format> onFile(Path file) {
        return format -> {
            try {
                return format.getFormatFileFilter().accept(file);
            } catch (IOException e) {
                return false;
            }
        };
    }
}
