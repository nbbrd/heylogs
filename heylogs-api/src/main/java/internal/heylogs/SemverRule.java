package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.service.ServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

@ServiceProvider
public final class SemverRule implements Rule {

    @Override
    public @NotNull String getId() {
        return "semver";
    }

    @Override
    public Failure validate(@NotNull Node node) {
        return node instanceof Heading ? validateSemVer((Heading) node) : NO_PROBLEM;
    }

    @Override
    public boolean isAvailable() {
        return Rule.isEnabled(System.getProperties(), getId());
    }

    @VisibleForTesting
    Failure validateSemVer(Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return NO_PROBLEM;
        }

        try {
            Version version = Version.parse(heading);
            if (version.isUnreleased()) {
                return NO_PROBLEM;
            }
            String ref = version.getRef();
            return Semver.isValid(ref)
                    ? NO_PROBLEM
                    : Failure
                    .builder()
                    .rule(this)
                    .message("Invalid semver format: '" + ref + "'")
                    .location(heading)
                    .build();
        } catch (IllegalArgumentException ex) {
            return NO_PROBLEM;
        }
    }
}
