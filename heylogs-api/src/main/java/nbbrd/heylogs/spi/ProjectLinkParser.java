package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;

@FunctionalInterface
public interface ProjectLinkParser extends ForgeLinkParser {

    @Override
    @NonNull
    ProjectLink parseForgeLink(@NonNull URL url) throws IllegalArgumentException;

    @Override
    default @Nullable ProjectLink parseForgeLinkOrNull(@NonNull URL url) throws IllegalArgumentException {
        try {
            return parseForgeLink(url);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    @StaticFactoryMethod
    static @NonNull ProjectLinkParser casting(@NonNull ForgeLinkParser parser) {
        return url -> {
            ForgeLink result = parser.parseForgeLink(url);
            if (result instanceof ProjectLink)
                return (ProjectLink) result;
            throw new IllegalArgumentException("Parsed link is not a project link: " + url);
        };
    }

    @StaticFactoryMethod
    static @NonNull ProjectLinkParser anyOf(@NonNull List<ProjectLinkParser> parsers) {
        return url -> {
            for (ProjectLinkParser parser : parsers) {
                try {
                    return parser.parseForgeLink(url);
                } catch (IllegalArgumentException ignore) {
                }
            }
            throw new IllegalArgumentException("None of the parsers could parse the URL: " + url);
        };
    }
}
