package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import org.semver4j.Semver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.TimeRange.toTimeRange;
import static nbbrd.heylogs.Util.illegalArgumentToNull;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Scanner {

    @StaticFactoryMethod
    public static @NonNull Scanner ofServiceLoader() {
        return builder()
                .formats(FormatLoader.load())
                .build();
    }

    @NonNull
    @lombok.Singular
    List<Format> formats;

    @NonNull
    @lombok.Builder.Default
    String formatId = FIRST_FORMAT_AVAILABLE;

    public @NonNull Status scan(@NonNull Node document) {
        Map<Boolean, List<Version>> versionByType = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(illegalArgumentToNull(Version::parse))
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(Version::isUnreleased));

        boolean compatibleWithSemver = isCompatibleWithSemver(versionByType.get(false));

        return Status
                .builder()
                .releaseCount(versionByType.get(false).size())
                .timeRange(versionByType.get(false).stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL))
                .compatibleWithSemver(compatibleWithSemver)
                .semverDetails(compatibleWithSemver ? getDetails(versionByType.get(false)) : "")
                .hasUnreleasedSection(versionByType.containsKey(true))
                .build();
    }

    private static boolean isCompatibleWithSemver(List<Version> releases) {
        return releases.stream().map(Version::getRef).allMatch(Semver::isValid);
    }

    private static String getDetails(List<Version> releases) {
        List<Semver> semvers = releases.stream().map(Version::getRef).map(Semver::parse).collect(toList());

        TreeMap<Semver.VersionDiff, List<Semver.VersionDiff>> diffs = IntStream.range(1, semvers.size())
                .mapToObj(i -> semvers.get(i).diff(semvers.get(i - 1)))
                .collect(groupingBy((Semver.VersionDiff o) -> o, TreeMap::new, toList()));

        return diffs
                .descendingMap()
                .entrySet()
                .stream()
                .map(entry -> entry.getValue().size() + " " + entry.getKey().toString())
                .collect(Collectors.joining(", ", " (", ")"));
    }

    public void formatStatus(@NonNull Appendable appendable, @NonNull String source, @NonNull Status status) throws IOException {
        getFormatById().formatStatus(appendable, source, status);
    }

    private Format getFormatById() throws IOException {
        return formats.stream()
                .filter(format -> formatId.equals(FIRST_FORMAT_AVAILABLE) || format.getFormatId().equals(formatId))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"));
    }

    private static final String FIRST_FORMAT_AVAILABLE = "";
}
