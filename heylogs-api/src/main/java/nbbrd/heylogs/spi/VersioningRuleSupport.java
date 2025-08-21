package nbbrd.heylogs.spi;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Version;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.Locale.ROOT;

@lombok.Builder(toBuilder = true)
public final class VersioningRuleSupport implements Rule {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    @lombok.Builder.Default
    private final @NonNull Predicate<Properties> availability = properties -> true;

    @lombok.Builder.Default
    private final @NonNull RuleSeverity severity = RuleSeverity.ERROR;

    private final @NonNull String versioningId;

    private final @NonNull BiPredicate<String, String> validator;

    @Override
    public @NonNull String getRuleId() {
        return id;
    }

    @Override
    public @NonNull String getRuleName() {
        return name;
    }

    @Override
    public @NonNull String getRuleModuleId() {
        return moduleId;
    }

    @Override
    public boolean isRuleAvailable() {
        return availability.test(System.getProperties());
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return severity;
    }

    @Override
    public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
        return versioningId.equals(context.getConfig().getVersioningId()) && node instanceof Heading
                ? validateVersioning((Heading) node, context.getConfig()) : NO_RULE_ISSUE;
    }

    private RuleIssue validateVersioning(Heading heading, Config config) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }

        try {
            Version version = Version.parse(heading);
            if (version.isUnreleased()) {
                return NO_RULE_ISSUE;
            }
            String ref = version.getRef();
            String format = Objects.toString(config.getVersioningArg(), "");
            return validator.test(format, ref)
                    ? NO_RULE_ISSUE
                    : RuleIssue
                    .builder()
                    .message(String.format(ROOT, "Invalid %s format: '%s'", versioningId, ref))
                    .location(heading)
                    .build();
        } catch (IllegalArgumentException ex) {
            return NO_RULE_ISSUE;
        }
    }
}
