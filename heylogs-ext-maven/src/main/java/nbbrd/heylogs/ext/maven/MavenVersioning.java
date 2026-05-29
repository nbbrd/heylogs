package nbbrd.heylogs.ext.maven;

import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.VersioningSupport.withoutArg;

@DirectImpl
@ServiceProvider
public final class MavenVersioning implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("maven")
            .name("Maven Versioning")
            .urlOf("https://maven.apache.org/pom.html#Version_Order_Specification")
            .moduleId("maven")
            .validator(arg -> arg == null ? null : "Maven versioning does not take any arguments")
            .predicate(withoutArg(MavenVersioning::validate))
            .comparator(withoutArg(MavenVersioning::compare))
            .familyMapper(withoutArg(MavenVersioning::toFamily))
            .build();

    // Matches MAJOR.MINOR[.INCREMENTAL][[-.]qualifier]
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "\\d+(\\.\\d+)+([-.][a-zA-Z0-9][\\w.-]*)?"
    );

    private static final Pattern FAMILY_PATTERN = Pattern.compile(
            "(\\d+\\.\\d+).*"
    );

    private static boolean validate(CharSequence text) {
        return VERSION_PATTERN.matcher(text).matches();
    }

    private static int compare(CharSequence a, CharSequence b) {
        if (!validate(a) || !validate(b)) {
            return 0; // incomparable
        }
        return new ComparableVersion(a.toString()).compareTo(new ComparableVersion(b.toString()));
    }

    private static String toFamily(CharSequence version) {
        if (!validate(version)) {
            return null;
        }
        Matcher matcher = FAMILY_PATTERN.matcher(version);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}

