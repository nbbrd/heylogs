package internal.heylogs.base;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Tagging;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@DirectImpl
@ServiceProvider
public final class PrefixTagging implements Tagging {

    @Override
    public @NonNull String getTaggingId() {
        return "prefix";
    }

    @Override
    public @NonNull String getTaggingName() {
        return "Prefix tagging";
    }

    @Override
    public @NonNull String getTaggingModuleId() {
        return "api";
    }

    @Override
    public Function<String, String> getTagFormatterOrNull(@Nullable String arg) {
        return arg != null ? (versionRef -> arg + versionRef) : null;
    }

    @Override
    public Function<String, String> getTagParserOrNull(@Nullable String arg) {
        return arg != null ? (tag -> tag.startsWith(arg) ? tag.substring(arg.length()) : null) : null;
    }
}
