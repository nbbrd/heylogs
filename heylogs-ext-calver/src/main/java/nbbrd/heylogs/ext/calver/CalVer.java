package nbbrd.heylogs.ext.calver;

import internal.heylogs.ext.calver.CalVerFormat;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

@DirectImpl
@ServiceProvider
public final class CalVer implements Versioning {

    static final String ID = "calver";

    @Override
    public @NonNull String getVersioningId() {
        return ID;
    }

    @Override
    public @NonNull String getVersioningName() {
        return "Calendar Versioning";
    }

    @Override
    public @NonNull String getVersioningModuleId() {
        return "calver";
    }

    @Override
    public @NonNull Predicate<CharSequence> getVersioningPredicate(@Nullable String arg) {
        return VersioningSupport.compose(CalVerFormat::parse, CalVerFormat::isValidVersion, arg);
    }
}
