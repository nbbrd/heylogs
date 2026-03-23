package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

import java.net.URL;

@FunctionalInterface
public interface CompareLinkParser extends ProjectLinkParser {

    @Override
    @NonNull
    CompareLink parseForgeLink(@NonNull URL url) throws IllegalArgumentException;

    @Override
    default @Nullable CompareLink parseForgeLinkOrNull(@NonNull URL url) throws IllegalArgumentException {
        try {
            return parseForgeLink(url);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    @StaticFactoryMethod
    static @NonNull CompareLinkParser casting(@NonNull ForgeLinkParser parser) {
        return url -> {
            ForgeLink result = parser.parseForgeLink(url);
            if (result instanceof CompareLink)
                return (CompareLink) result;
            throw new IllegalArgumentException("Parsed link is not a compare link: " + url);
        };
    }
}
