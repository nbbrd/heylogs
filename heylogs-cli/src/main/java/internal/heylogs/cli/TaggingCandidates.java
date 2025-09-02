package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.spi.Tagging;
import nbbrd.heylogs.spi.TaggingLoader;

import java.util.Iterator;

public final class TaggingCandidates implements Iterable<String> {

    @Override
    public @NonNull Iterator<String> iterator() {
        return TaggingLoader.load()
                .stream()
                .map(Tagging::getTaggingId)
                .iterator();
    }
}
