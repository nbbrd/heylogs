package nbbrd.heylogs.ext.calver;

import internal.heylogs.ext.calver.CalVerFormat;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Validator;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;

import static nbbrd.heylogs.spi.VersioningSupport.compilingArg;

@DirectImpl
@ServiceProvider
public final class CalVer implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("calver")
            .name("Calendar Versioning")
            .moduleId("calver")
            .validator(Validator.of(CalVerFormat::parse))
            .predicate(compilingArg(CalVerFormat::parse, CalVerFormat::isValidVersion))
            .build();
}
