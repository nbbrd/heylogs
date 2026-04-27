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
            .urlOf("https://semver.org/")
            .moduleId("semver")
            .validator(arg -> arg == null ? null : "Semver does not take any arguments")
            .predicate(withoutArg(SemVer::validate))
            .comparator(withoutArg(SemVer::compare))
            .familyMapper(withoutArg(SemVer::toFamily))
            .build();

    private static boolean validate(CharSequence text) {
        return Semver.isValid(text.toString());
    }

    private static int compare(CharSequence a, CharSequence b) {
        if (!validate(a) || !validate(b)) {
            return 0; // incomparable
        }
        return new Semver(a.toString()).compareTo(new Semver(b.toString()));
    }

    private static String toFamily(CharSequence version) {
        if (!validate(version)) {
            return null;
        }
        Semver semver = new Semver(version.toString());
        return semver.getMajor() + "." + semver.getMinor();
    }
}
