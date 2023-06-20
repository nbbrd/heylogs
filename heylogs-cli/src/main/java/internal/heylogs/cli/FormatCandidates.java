package internal.heylogs.cli;

import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;

import java.util.Iterator;

public final class FormatCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return FormatLoader.load()
                .stream()
                .map(Format::getId)
                .iterator();
    }
}
