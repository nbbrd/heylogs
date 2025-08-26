package internal.heylogs.base;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningBatch;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static nbbrd.heylogs.spi.VersioningSupport.compilingArg;

@DirectImpl
@ServiceProvider
public final class BaseVersionings implements VersioningBatch {

    @Override
    public @NonNull Stream<Versioning> getProviders() {
        return Stream.of(REGEX_VERSIONING);
    }

    @VisibleForTesting
    public static final VersioningSupport REGEX_VERSIONING = VersioningSupport
            .builder()
            .id("regex")
            .name("Regex Versioning")
            .moduleId("api")
            .validation(compilingArg(Pattern::compile, (pattern, text) -> pattern.matcher(text).matches()))
            .build();
}
