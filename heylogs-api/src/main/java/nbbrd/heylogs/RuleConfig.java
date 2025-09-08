package nbbrd.heylogs;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleLoader;
import nbbrd.heylogs.spi.RuleSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleConfig {

    @StaticFactoryMethod
    public static @NonNull RuleConfig parse(@NonNull CharSequence text) throws IllegalArgumentException {
        String textString = text.toString();
        int index = textString.indexOf(':');
        return index != -1
                ? of(textString.substring(0, index), RuleSeverity.valueOf(textString.substring(index + 1)))
                : of(textString, null);
    }

    @StaticFactoryMethod
    public static @NonNull RuleConfig of(@NonNull String id) throws IllegalArgumentException {
        if (!RuleLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID '" + id + "'");
        }
        return new RuleConfig(id, null);
    }

    @StaticFactoryMethod
    public static @NonNull RuleConfig of(@NonNull String id, @Nullable RuleSeverity severity) throws IllegalArgumentException {
        if (!RuleLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID '" + id + "'");
        }
        return new RuleConfig(id, severity);
    }

    @NonNull
    String id;

    @Nullable
    RuleSeverity severity;

    @Override
    public String toString() {
        return id + (severity != null ? (":" + severity) : "");
    }

    public boolean isCompatibleWith(@NonNull Rule other) {
        return this.id.equals(other.getRuleId());
    }
}
