package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;

@FunctionalInterface
public interface ForgeLinkResolver {

    @NonNull
    ForgeLink resolveForgeLink(@NonNull URL url, @NonNull CharSequence link) throws IllegalArgumentException;

    default @Nullable ForgeLink resolveForgeLinkOrNull(@NonNull URL url, @NonNull CharSequence link) throws IllegalArgumentException {
        try {
            return resolveForgeLink(url, link);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }
}
