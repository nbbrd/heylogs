package nbbrd.heylogs.ext.semver;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

import java.util.function.Predicate;

@DirectImpl
@ServiceProvider
public final class SemVer implements Versioning {

    static final String ID = "semver";

    @Override
    public @NonNull String getVersioningId() {
        return ID;
    }

    @Override
    public @NonNull String getVersioningName() {
        return "Semantic Versioning";
    }

    @Override
    public @NonNull String getVersioningModuleId() {
        return "semver";
    }

    @Override
    public @NonNull Predicate<CharSequence> getVersioningPredicate(@Nullable String ignore) {
        return text -> Semver.isValid(text.toString());
    }
}
