package internal.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Versioning;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class VersioningSupport {

    private VersioningSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Stream<Versioning> versioningStreamOf(List<Versioning> list, List<Version> releases, String versioningArg) {
        return list.stream().filter(versioning ->
                releases.stream()
                        .map(Version::getRef)
                        .allMatch(versioning.getVersioningPredicate(versioningArg))
        );
    }

    public static @NonNull Predicate<Versioning> onVersioningId(@NonNull String id) {
        return versioning -> versioning.getVersioningId().equals(id);
    }
}
