package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.TypeOfChange;

import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

public final class TypeOfChangeCandidates implements Iterable<String> {

    @Override
    public @NonNull Iterator<String> iterator() {
        return Stream.of(TypeOfChange.values())
                .map(type -> type.name().toLowerCase(Locale.ROOT))
                .iterator();
    }
}
