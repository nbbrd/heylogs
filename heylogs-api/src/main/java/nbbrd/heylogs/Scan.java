package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import org.semver4j.Semver;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@lombok.Value
public class Scan {

    public static Scan of(Node document) {
        Map<Boolean, List<Version>> versionByType = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .map(Version::parse)
                .collect(Collectors.partitioningBy(Version::isUnreleased));

        return new Scan(
                versionByType.get(false).size(),
                TimeRange.of(versionByType.get(false)),
                SemverSummary.of(versionByType.get(false)),
                versionByType.containsKey(true)
        );
    }

    int releaseCount;
    TimeRange timeRange;
    SemverSummary semverSummary;
    boolean hasUnreleasedSection;

    @lombok.Value
    public static class TimeRange {

        public static final TimeRange NONE = new TimeRange(LocalDate.MIN, LocalDate.MAX);

        public static TimeRange of(List<Version> versions) {
            return versions.isEmpty() ? NONE : new TimeRange(versions.get(versions.size() - 1).getDate(), versions.get(0).getDate());
        }

        LocalDate fist;
        LocalDate last;
    }

    @lombok.Value
    public static class SemverSummary {

        public static SemverSummary of(List<Version> releases) {
            if (!isCompatibleWithSemver(releases)) {
                return new SemverSummary(false, "");
            }

            List<Semver> semvers = releases.stream().map(Version::getRef).map(Semver::parse).collect(toList());

            SortedMap<Semver.VersionDiff, List<Semver.VersionDiff>> diffs = IntStream.range(1, semvers.size())
                    .mapToObj(i -> semvers.get(i).diff(semvers.get(i - 1)))
                    .collect(groupingBy((Semver.VersionDiff o) -> o, TreeMap::new, toList()));

            String details = diffs
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getValue().size() + " " + entry.getKey().toString())
                    .collect(Collectors.joining(", ", " (", ")"));

            return new SemverSummary(true, details);
        }

        private static boolean isCompatibleWithSemver(List<Version> releases) {
            return releases.stream().map(Version::getRef).allMatch(Semver::isValid);
        }

        boolean compatibleWithSemver;
        String details;
    }
}
