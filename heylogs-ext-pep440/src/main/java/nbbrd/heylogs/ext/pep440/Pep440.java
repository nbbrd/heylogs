package nbbrd.heylogs.ext.pep440;

import internal.heylogs.ext.pep440.Pep440Version;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;

import static nbbrd.heylogs.spi.VersioningSupport.withoutArg;

@DirectImpl
@ServiceProvider
public final class Pep440 implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("pep440")
            .name("Python PEP 440")
            .urlOf("https://peps.python.org/pep-0440/")
            .moduleId("pep440")
            .validator(arg -> arg == null ? null : "PEP 440 does not take any arguments")
            .predicate(withoutArg(Pep440Version::isValid))
            .comparator(withoutArg(Pep440Version::compare))
            .familyMapper(withoutArg(Pep440Version::toFamily))
            .build();
}

