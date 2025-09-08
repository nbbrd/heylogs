package internal.heylogs.base;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Tagging;
import nbbrd.heylogs.spi.TaggingSupport;
import nbbrd.service.ServiceProvider;

@DirectImpl
@ServiceProvider
public final class PrefixTagging implements Tagging {

    @lombok.experimental.Delegate
    private final Tagging delegate = TaggingSupport
            .builder()
            .id("prefix")
            .name("Prefix tagging")
            .moduleId("api")
            .validator(PrefixTagging::validateArg)
            .formatter(PrefixTagging::formatTag)
            .parser(PrefixTagging::parseTag)
            .build();

    private static String validateArg(String arg) {
        return arg == null || arg.isEmpty() ? "Prefix cannot be null or empty" : null;
    }

    private static String formatTag(String arg, String versionRef) {
        return arg + versionRef;
    }

    private static String parseTag(String arg, @NonNull String tag) {
        if (tag.startsWith(arg)) return tag.substring(arg.length());
        throw new IllegalArgumentException("Tag does not start with the specified prefix '" + arg + "'");
    }
}
