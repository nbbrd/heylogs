package nbbrd.heylogs.ext.semver;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.service.ServiceProvider;
import org.semver4j.Semver;

@DirectImpl
@ServiceProvider
public final class SemVer implements Versioning {

    @Override
    public @NonNull String getVersioningId() {
        return "semver";
    }

    @Override
    public @NonNull String getVersioningName() {
        return "Semantic Versioning";
    }

    @Override
    public boolean isValidVersion(@NonNull CharSequence text) {
        return Semver.isValid(text.toString());
    }
}
