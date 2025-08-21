package nbbrd.heylogs.ext.calver;

import internal.heylogs.ext.calver.CalVerFormat;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.heylogs.spi.VersioningRuleSupport;
import nbbrd.service.ServiceProvider;

import java.util.stream.Stream;

@DirectImpl
@ServiceProvider
public final class CalVerRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(CALVER_FORMAT);
    }

    @VisibleForTesting
    static final Rule CALVER_FORMAT = VersioningRuleSupport
            .builder()
            .id("calver-format")
            .name("Calendar Versioning format")
            .moduleId("calver")
            .versioningId(CalVer.ID)
            .validator((format, ref) -> CalVerFormat.parse(format).isValidVersion(ref))
            .build();
}
