package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public @NonNull List<Problem> validate(@NonNull Document doc) {
        return concat(Stream.of(doc), Nodes.of(Node.class).descendants(doc))
                .flatMap(node -> rules.stream().map(rule -> getProblemOrNull(node, rule)).filter(Objects::nonNull))
                .collect(toList());
    }

    public @NonNull List<Resource> getResources() {
        return concat(
                rules.stream().map(Heylogs::asResource),
                formats.stream().map(Heylogs::asResource),
                versionings.stream().map(Heylogs::asResource)
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

    private static Problem getProblemOrNull(Node node, Rule rule) {
        RuleIssue ruleIssueOrNull = rule.getRuleIssueOrNull(node);
        return ruleIssueOrNull != null ? Problem.builder().rule(rule).issue(ruleIssueOrNull).build() : null;
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
        Map<Boolean, List<Version>> versionByType = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(illegalArgumentToNull(Version::parse))
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(Version::isUnreleased));

        List<String> compatibilities = getCompatibilities(versionByType.get(false));

        return Summary
                .builder()
                .releaseCount(versionByType.get(false).size())
                .timeRange(versionByType.get(false).stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL))
                .compatibilities(compatibilities)
                .hasUnreleasedSection(versionByType.containsKey(true))
                .build();
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
