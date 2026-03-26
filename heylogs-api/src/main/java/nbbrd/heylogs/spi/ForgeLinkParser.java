package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;

@FunctionalInterface
public interface ForgeLinkParser {

    @NonNull
    ForgeLink parseForgeLink(@NonNull URL url) throws IllegalArgumentException;

    default @Nullable ForgeLink parseForgeLinkOrNull(@NonNull URL url) throws IllegalArgumentException {
        try {
            return parseForgeLink(url);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }
}
