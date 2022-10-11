package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import org.semver4j.Semver;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.TimeRange.toTimeRange;

@lombok.Value
public class Scan {

    public static Scan of(Node document) {
        Map<Boolean, List<Version>> versionByType = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(Version::parse)
                .collect(Collectors.partitioningBy(Version::isUnreleased));

        boolean compatibleWithSemver = isCompatibleWithSemver(versionByType.get(false));

        return new Scan(
                versionByType.get(false).size(),
                versionByType.get(false).stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL),
                compatibleWithSemver,
                compatibleWithSemver ? getDetails(versionByType.get(false)) : "",
                versionByType.containsKey(true)
        );
    }

    int releaseCount;
    TimeRange timeRange;
    boolean compatibleWithSemver;
    String semverDetails;
    boolean hasUnreleasedSection;

    private static boolean isCompatibleWithSemver(List<Version> releases) {
        return releases.stream().map(Version::getRef).allMatch(Semver::isValid);
    }

    private static String getDetails(List<Version> releases) {
        List<Semver> semvers = releases.stream().map(Version::getRef).map(Semver::parse).collect(toList());

        SortedMap<Semver.VersionDiff, List<Semver.VersionDiff>> diffs = IntStream.range(1, semvers.size())
                .mapToObj(i -> semvers.get(i).diff(semvers.get(i - 1)))
                .collect(groupingBy((Semver.VersionDiff o) -> o, TreeMap::new, toList()));

        return diffs
                .entrySet()
                .stream()
                .map(entry -> entry.getValue().size() + " " + entry.getKey().toString())
                .collect(Collectors.joining(", ", " (", ")"));
    }
}
