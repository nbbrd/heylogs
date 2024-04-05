package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;
import org.semver4j.Semver;

@ServiceProvider
public final class SemverRule implements Rule {

    @Override
    public @NonNull String getRuleId() {
        return "semver";
    }

    @Override
    public @NonNull String getRuleName() {
        return "Semantic Versioning format";
    }

    @Override
    public RuleIssue getRuleIssueOrNull(@NonNull Node node) {
        return node instanceof Heading ? validateSemVer((Heading) node) : NO_RULE_ISSUE;
    }

    @Override
    public boolean isRuleAvailable() {
        return Rule.isEnabled(System.getProperties(), getRuleId());
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return RuleSeverity.ERROR;
    }

    @VisibleForTesting
    RuleIssue validateSemVer(Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }

        try {
            Version version = Version.parse(heading);
            if (version.isUnreleased()) {
                return NO_RULE_ISSUE;
            }
            String ref = version.getRef();
            return Semver.isValid(ref)
                    ? NO_RULE_ISSUE
                    : RuleIssue
                    .builder()
                    .message("Invalid semver format: '" + ref + "'")
                    .location(heading)
                    .build();
        } catch (IllegalArgumentException ex) {
            return NO_RULE_ISSUE;
        }
    }
}
