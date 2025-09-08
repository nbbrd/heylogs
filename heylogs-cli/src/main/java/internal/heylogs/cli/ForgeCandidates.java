package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeLoader;

import java.util.Iterator;

public final class ForgeCandidates implements Iterable<String> {

    @Override
    public @NonNull Iterator<String> iterator() {
        return ForgeLoader.load()
                .stream()
                .map(Forge::getForgeId)
                .iterator();
    }
}
