package nbbrd.heylogs.spi;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.BiFunction;

@lombok.Builder(toBuilder = true)
public final class RuleSupport implements Rule {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    @lombok.Builder.Default
    private final boolean availability = true;

    @lombok.Builder.Default
    private final @NonNull RuleSeverity severity = RuleSeverity.ERROR;

    private final @NonNull BiFunction<Node, RuleContext, RuleIssue> issueProvider;

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
        return availability;
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return severity;
    }

    @Override
    public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
        return issueProvider.apply(node, context);
    }

    public static final class Builder {

        public @NonNull Builder id(@NonNull String id) {
            this.id = checkRuleId(id);
            return this;
        }
    }

    @MightBeGenerated
    public static String checkRuleId(@NonNull String id) {
        if (!RuleLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid rule id '" + id + "', should follow pattern " + RuleLoader.ID_PATTERN.pattern());
        }
        return id;
    }

    public static @NonNull String nameToId(@NonNull Enum<?> o) {
        return o.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
