package nbbrd.heylogs.ext.semver;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.heylogs.spi.VersioningRuleSupport;
import nbbrd.service.ServiceProvider;
import org.semver4j.Semver;

import java.util.stream.Stream;

@DirectImpl
@ServiceProvider
public final class SemVerRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(SEMVER_FORMAT);
    }

    @VisibleForTesting
    static final Rule SEMVER_FORMAT = VersioningRuleSupport
            .builder()
            .id("semver-format")
            .name("Semantic Versioning format")
            .moduleId("semver")
            .versioningId(SemVer.ID)
            .validator((format, ref) -> Semver.isValid(ref))
            .build();
}
