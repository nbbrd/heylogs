package internal.heylogs.spi;

import nbbrd.heylogs.Config;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Versioning;

import java.util.List;
import java.util.stream.Stream;

public final class VersioningSupport {

    private VersioningSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Stream<Versioning> versioningStreamOf(List<Versioning> list, List<Version> releases, Config config) {
        return list.stream()
                .filter(versioning -> releases.stream().allMatch(release -> versioning.isValidVersion(release.getRef(), config)));
    }
}
