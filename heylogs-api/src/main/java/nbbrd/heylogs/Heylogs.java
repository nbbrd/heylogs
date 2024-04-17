package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ChangelogNodes;
import internal.heylogs.GuidingPrinciples;
import internal.heylogs.URLExtractor;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.TimeRange.toTimeRange;
import static nbbrd.heylogs.Util.illegalArgumentToNull;

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

    public @NonNull List<Problem> validate(@NonNull Document doc) {
        return getProblemStream(doc, rules).collect(toList());
    }

    public @NonNull List<Resource> getResources() {
        return concat(
                rules.stream().map(Heylogs::asResource),
                formats.stream().map(Heylogs::asResource),
                versionings.stream().map(Heylogs::asResource),
                forges.stream().map(Heylogs::asResource)
        )
                .sorted(comparing(Resource::getType).thenComparing(Resource::getCategory).thenComparing(Resource::getId))
                .collect(toList());
    }

    private static Resource asResource(Rule rule) {
        return Resource
                .builder()
                .type("rule")
                .category(rule.getRuleCategory())
                .id(rule.getRuleId())
                .name(rule.getRuleName())
                .build();
    }

    private static Resource asResource(Format format) {
        return Resource
                .builder()
                .type("format")
                .category(format.getFormatCategory())
                .id(format.getFormatId())
                .name(format.getFormatName())
                .build();
    }

    private static Resource asResource(Versioning versioning) {
        return Resource
                .builder()
                .type("versioning")
                .category("main")
                .id(versioning.getVersioningId())
                .name(versioning.getVersioningName())
                .build();
    }

    private static Resource asResource(Forge forge) {
        return Resource
                .builder()
                .type("forge")
                .category("main")
                .id(forge.getForgeId())
                .name(forge.getForgeName())
                .build();
    }

    public void formatProblems(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        getFormatById(formatId).formatProblems(appendable, list);
    }

    public void formatStatus(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        getFormatById(formatId).formatStatus(appendable, list);
    }

    public void formatResources(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        getFormatById(formatId).formatResources(appendable, list);
    }

    public @NonNull Summary scan(@NonNull Node document) {
        if (getProblemStream(document, asList(GuidingPrinciples.values())).findFirst().isPresent())
            return Summary.builder().valid(false).build();

        List<Version> releases = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(illegalArgumentToNull(Version::parse))
                .filter(Objects::nonNull)
                .filter(version -> !version.isUnreleased())
                .collect(toList());

        long unreleasedChanges = ChangelogNodes.getUnreleasedHeading(document)
                .map(ChangelogNodes::getBulletListsByTypeOfChange)
                .map(o -> o.values().stream().mapToLong(List::size).sum())
                .orElse(0L);

        Optional<String> forgeURL = getLatestVersionURL(document);
        Forge forgeOrNull = forgeURL.flatMap(this::getForge).orElse(null);

        return Summary
                .builder()
                .valid(true)
                .releaseCount(releases.size())
                .timeRange(releases.stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL))
                .compatibilities(getCompatibilities(releases))
                .unreleasedChanges((int) unreleasedChanges)
                .forgeName(forgeOrNull != null ? forgeOrNull.getForgeName() : null)
                .forgeURL(forgeURL.map(x -> getBaseURL(forgeOrNull, x)).orElse(null))
                .build();
    }

    private URL getBaseURL(Forge forgeOrNull, CharSequence url) {
        return forgeOrNull != null ? forgeOrNull.getBaseURL(url) : URLExtractor.baseOf(URLExtractor.urlOf(url));
    }

    private Optional<Forge> getForge(String url) {
        return forges.stream().filter(forge -> forge.isCompareLink(url)).findFirst();
    }

    private static Optional<String> getLatestVersionURL(Node document) {
        return Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(heading -> {
                    ReferenceRepository repository = Parser.REFERENCES.get(heading.getDocument());
                    String normalizeRef = repository.normalizeKey(Version.parse(heading).getRef());
                    return Objects.requireNonNull(repository.get(normalizeRef)).getUrl().toString();
                })
                .findFirst();
    }

    private List<String> getCompatibilities(List<Version> releases) {
        return versionings.stream()
                .filter(versioning -> releases.stream().allMatch(release -> versioning.isValidVersion(release.getRef())))
                .map(Versioning::getVersioningName)
                .collect(toList());
    }

    private Format getFormatById(String formatId) throws IOException {
        return formats.stream()
                .filter(format -> formatId.equals(FIRST_FORMAT_AVAILABLE) || format.getFormatId().equals(formatId))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"));
    }

    @VisibleForTesting
    static Stream<Problem> getProblemStream(Node root, List<Rule> rules) {
        return Nodes.walk(root)
                .flatMap(node -> rules.stream().map(rule -> getProblemOrNull(node, rule)).filter(Objects::nonNull));
    }

    private static Problem getProblemOrNull(Node node, Rule rule) {
        RuleIssue ruleIssueOrNull = rule.getRuleIssueOrNull(node);
        return ruleIssueOrNull != null ? Problem.builder().rule(rule).issue(ruleIssueOrNull).build() : null;
    }

    public static final String FIRST_FORMAT_AVAILABLE = "";

    @MightBePromoted
    @SafeVarargs
    private static <T> Stream<T> concat(Stream<T> first, Stream<T>... rest) {
        Stream<T> result = first;
        for (Stream<T> next : rest) {
            result = Stream.concat(result, next);
        }
        return result;
    }
}
