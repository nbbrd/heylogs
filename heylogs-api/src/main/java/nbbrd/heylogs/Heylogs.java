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
import nbbrd.design.VisibleForTesting;
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

import static internal.heylogs.spi.FormatSupport.onFormatFileFilter;
import static internal.heylogs.spi.FormatSupport.onFormatId;
import static internal.heylogs.spi.RuleSupport.problemStreamOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.TimeRange.toTimeRange;
import static nbbrd.heylogs.spi.ForgeSupport.onCompareLink;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FILTER;

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
                .taggings(TaggingLoader.load())
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

    @NonNull
    @lombok.Singular
    List<Tagging> taggings;

    public @NonNull List<Problem> check(@NonNull Document document, @NonNull Config config) {
        checkConfig(config);

        RuleContext context = RuleContext.builder().config(config).forges(forges).versionings(versionings).taggings(taggings).build();
        return problemStreamOf(document, rules, context).collect(toList());
    }

    public void extract(@NonNull Document document, @NonNull Filter filter) {
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

    public @NonNull List<Resource> list() {
        return concat(
                rules.stream().map(Resource::of),
                formats.stream().map(Resource::of),
                versionings.stream().map(Resource::of),
                forges.stream().map(Resource::of),
                taggings.stream().map(Resource::of)
        ).sorted(Resource.DEFAULT_COMPARATOR).collect(toList());
    }

    public void release(@NonNull Document document, @NonNull Version newVersion, @NonNull Config config) throws IllegalArgumentException {
        checkConfig(config);

        Predicate<CharSequence> versioningPredicate = getVersioningPredicate(config.getVersioning());
        if (versioningPredicate != null && !versioningPredicate.test(newVersion.getRef())) {
            throw new IllegalArgumentException("Invalid version '" + newVersion.getRef() + "' for versioning '" + config.getVersioning() + "'");
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

        Forge forge = config.getForge() != null
                ? findForge(config.getForge()::isCompatibleWith).orElseThrow(() -> new IllegalArgumentException("Cannot find forge with id '" + config.getForge().getId() + "'"))
                : findForge(onCompareLink(unreleased.getURL())).orElseThrow(() -> new IllegalArgumentException("Cannot determine forge"));

        String releaseTag = getTag(config.getTagging(), newVersion.getRef());
        URL releaseURL = forge.getCompareLink(unreleased.getURL()).derive(releaseTag).toURL();
        VersionHeading release = VersionHeading.of(newVersion, releaseURL);

        URL updatedURL = forge.getCompareLink(releaseURL).derive("HEAD").toURL();
        VersionHeading updated = VersionHeading.of(unreleased.getSection(), updatedURL);

        changelog.getRepository().putRawKey(release.getReference().getReference(), release.getReference());
        changelog.getRepository().putRawKey(updated.getReference().getReference(), updated.getReference());

        unreleased.getHeading().appendChild(release.getHeading());
        unreleased.getReference().insertAfter(release.getReference());
        unreleased.getReference().insertBefore(updated.getReference());
        unreleased.getReference().unlink();
    }

    public @NonNull Summary scan(@NonNull Document document) {
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
        Forge forgeOrNull = findForge(onCompareLink(first.getURL())).orElse(null);

        return Summary
                .builder()
                .valid(true)
                .releaseCount(releases.size())
                .timeRange(releases.stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL))
                .compatibilities(versioningStreamOf(versionings, releases).map(Versioning::getVersioningName).collect(toList()))
                .unreleasedChanges((int) unreleasedChanges)
                .forgeName(forgeOrNull != null ? forgeOrNull.getForgeName() : null)
                .forgeURL(getForgeURL(forgeOrNull, first.getURL()))
                .build();
    }

    public void formatProblems(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        findFormat(onFormatId(formatId))
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatProblems(appendable, list);
    }

    public void formatStatus(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        findFormat(onFormatId(formatId))
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatStatus(appendable, list);
    }

    public void formatResources(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        findFormat(onFormatId(formatId))
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatResources(appendable, list);
    }

    public @NonNull Optional<String> getFormatIdByFile(@NonNull Path file) {
        return findFormat(onFormatFileFilter(file)).map(Format::getFormatId);
    }

    private URL getForgeURL(Forge forgeOrNull, URL url) {
        return forgeOrNull != null ? forgeOrNull.getCompareLink(url).getProjectURL() : URLExtractor.baseOf(url);
    }

    private Optional<Rule> findRule(@NonNull Predicate<Rule> predicate) {
        return rules.stream().filter(predicate).findFirst();
    }

    private Optional<Tagging> findTagging(@NonNull Predicate<Tagging> predicate) {
        return taggings.stream().filter(predicate).findFirst();
    }

    private Optional<Forge> findForge(@NonNull Predicate<Forge> predicate) {
        return forges.stream().filter(predicate).findFirst();
    }

    private Optional<Versioning> findVersioning(@NonNull Predicate<Versioning> predicate) {
        return versionings.stream().filter(predicate).findFirst();
    }

    private Optional<Format> findFormat(@NonNull Predicate<Format> predicate) {
        return formats.stream().filter(predicate).findFirst();
    }

    public static final String FIRST_FORMAT_AVAILABLE = "";

    private static boolean isNotValidAgainstGuidingPrinciples(Document document) {
        return problemStreamOf(document, asList(GuidingPrinciples.values()), RuleContext.DEFAULT).findFirst().isPresent();
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

    private static Stream<Versioning> versioningStreamOf(List<Versioning> list, List<Version> releases) {
        return list.stream()
                .filter(versioning -> versioning.getVersioningArgValidator().apply(null) == null)
                .filter(versioning -> {
                            Predicate<CharSequence> predicate = versioning.getVersioningPredicateOrNull(null);
                            return predicate != NO_VERSIONING_FILTER && releases.stream().map(Version::getRef).allMatch(predicate);
                        }
                );
    }

    private @Nullable Predicate<CharSequence> getVersioningPredicate(@Nullable VersioningConfig versioningConfig) {
        if (versioningConfig != null) {
            Versioning versioning = findVersioning(versioningConfig::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find versioning with id '" + versioningConfig.getId() + "'"));
            Predicate<CharSequence> predicate = versioning.getVersioningPredicateOrNull(versioningConfig.getArg());
            if (predicate == NO_VERSIONING_FILTER) {
                throw new IllegalArgumentException("Invalid version argument '" + versioningConfig.getArg() + "'");
            }
            return predicate;
        }
        return null;
    }

    private @NonNull String getTag(@Nullable TaggingConfig config, @NonNull String versionRef) {
        if (config != null) {
            Converter<String, String> tagFormatterOrNull = findTagging(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find tagging with id '" + config.getId() + "'"))
                    .getTagFormatterOrNull(config.getArg());
            if (tagFormatterOrNull != Tagging.CONVERSION_NOT_SUPPORTED) {
                return tagFormatterOrNull.apply(versionRef);
            }
        }
        return versionRef;
    }

    @VisibleForTesting
    void checkConfig(@NonNull Config config) throws IllegalArgumentException {
        checkVersioningConfig(config.getVersioning());
        checkTaggingConfig(config.getTagging());
        checkRuleConfigs(config.getRules());
        checkDomainConfigs(config.getDomains());
    }

    private void checkVersioningConfig(VersioningConfig config) throws IllegalArgumentException {
        if (config != null) {
            String error = findVersioning(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find versioning with id '" + config.getId() + "'"))
                    .getVersioningArgValidator()
                    .apply(config.getArg());
            if (error != null) {
                throw new IllegalArgumentException("Invalid versioning argument '" + config.getArg() + "': " + error);
            }
        }
    }

    private void checkTaggingConfig(TaggingConfig config) throws IllegalArgumentException {
        if (config != null) {
            String error = findTagging(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find tagging with id '" + config.getId() + "'"))
                    .getTaggingArgValidator()
                    .apply(config.getArg());
            if (error != null) {
                throw new IllegalArgumentException("Invalid tagging argument '" + config.getArg() + "': " + error);
            }
        }
    }

    private void checkRuleConfigs(List<RuleConfig> configs) throws IllegalArgumentException {
        for (RuleConfig config : configs) {
            findRule(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find rule with id '" + config.getId() + "'"));
        }
    }

    private void checkDomainConfigs(List<DomainConfig> configs) throws IllegalArgumentException {
        for (DomainConfig config : configs) {
            findForge(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find forge with id '" + config.getForgeId() + "'"));
        }
    }
}
