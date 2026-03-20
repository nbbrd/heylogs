package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ForgeRefParser {

    @NonNull
    ForgeRef parseForgeRef(@NonNull CharSequence value) throws IllegalArgumentException;

    default @Nullable ForgeRef parseForgeRefOrNull(@NonNull CharSequence value) throws IllegalArgumentException {
        try {
            return parseForgeRef(value);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }
}
