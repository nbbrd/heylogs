package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningLoader;

import java.util.Iterator;

public final class VersioningCandidates implements Iterable<String> {

    @Override
    public @NonNull Iterator<String> iterator() {
        return VersioningLoader.load()
                .stream()
                .map(Versioning::getVersioningId)
                .iterator();
    }
}
