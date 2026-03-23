package nbbrd.heylogs.spi;

import lombok.NonNull;

@FunctionalInterface
public interface CompareLinkConverter {

    @NonNull
    CompareLink convert(@NonNull ProjectLink link) throws IllegalArgumentException;
}
