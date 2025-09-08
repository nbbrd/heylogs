package nbbrd.heylogs.ext.semver;

import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;
import org.semver4j.Semver;

import static nbbrd.heylogs.spi.VersioningSupport.withoutArg;

@DirectImpl
@ServiceProvider
public final class SemVer implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("semver")
            .name("Semantic Versioning")
            .moduleId("semver")
            .validator(arg -> arg == null ? null : "Semver does not take any arguments")
            .predicate(withoutArg(text -> Semver.isValid(text.toString())))
            .build();
}
