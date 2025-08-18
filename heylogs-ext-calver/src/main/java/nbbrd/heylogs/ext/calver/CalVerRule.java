package nbbrd.heylogs.ext.calver;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ext.calver.CalVerFormat;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;

@DirectImpl
@ServiceProvider
public final class CalVerRule implements Rule {

    @Override
    public @NonNull String getRuleId() {
        return "calver";
    }

    @Override
    public @NonNull String getRuleName() {
        return "Calendar Versioning format";
    }

    @Override
    public @NonNull String getRuleModuleId() {
        return "calver";
    }

    @Override
    public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
        return CalVer.ID.equals(config.getVersioningId()) && node instanceof Heading
                ? validateCalVer((Heading) node, config) : NO_RULE_ISSUE;
    }

    @Override
    public boolean isRuleAvailable() {
        return true;
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return RuleSeverity.ERROR;
    }

    private RuleIssue validateCalVer(Heading heading, Config config) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }

        try {
            Version version = Version.parse(heading);
            if (version.isUnreleased()) {
                return NO_RULE_ISSUE;
            }
            String ref = version.getRef();
            String format = config.getVersioningArg();
            if (format == null) {
                return NO_RULE_ISSUE;
            }
            return CalVerFormat.parse(format).isValidVersion(ref)
                    ? NO_RULE_ISSUE
                    : RuleIssue
                    .builder()
                    .message("Invalid calver format: '" + ref + "'")
                    .location(heading)
                    .build();
        } catch (IllegalArgumentException ex) {
            return NO_RULE_ISSUE;
        }
    }
}
