package nbbrd.heylogs.ext.calver;

import internal.heylogs.ext.calver.CalVerFormat;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.service.ServiceProvider;

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
    public boolean isValidVersion(@NonNull CharSequence text, @NonNull Config config) {
        String format = config.getVersioningArg();
        if (format != null) {
            try {
                return CalVerFormat.parse(format).isValidVersion(text);
            } catch (IllegalArgumentException ignore) {
            }
        }
        return false;
    }
}
