package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import org.semver4j.Semver;

@ServiceProvider
public final class SemverRule implements Rule {

    @Override
    public String getName() {
        return "semver";
    }

    @Override
    public Failure validate(Node node) {
        return node instanceof Heading ? validateSemVer((Heading) node) : null;
    }

    @Override
    public boolean isAvailable() {
        return Rule.isEnabled(System.getProperties(), getName());
    }

    @VisibleForTesting
    Failure validateSemVer(Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return null;
        }

        try {
            Version version = Version.parse(heading);
            if (version.isUnreleased()) {
                return null;
            }
            String ref = version.getRef();
            return Semver.isValid(ref) ? null : Failure.of(this, "Invalid semver format: '" + ref + "'", heading);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
