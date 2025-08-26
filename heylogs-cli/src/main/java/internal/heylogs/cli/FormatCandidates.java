package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;

import java.util.Iterator;

public final class FormatCandidates implements Iterable<String> {

    @Override
    public @NonNull Iterator<String> iterator() {
        return FormatLoader.load()
                .stream()
                .map(Format::getFormatId)
                .iterator();
    }
}
