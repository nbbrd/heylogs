package internal.heylogs.git;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.util.regex.Pattern;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hash {

    @StaticFactoryMethod
    public static @NonNull Hash parse(@NonNull CharSequence text) {
        if (!HASH_PATTERN.matcher(text).matches())
            throw new IllegalArgumentException("Invalid hash format: " + text);
        return new Hash(text.toString());
    }

    @NonNull
    String value;

    @Override
    public String toString() {
        return value;
    }

    public @NonNull Hash toShort() {
        return new Hash(value.substring(0, 7));
    }

    public boolean isCompatibleWith(@NonNull Hash other) {
        return other.getValue().startsWith(this.getValue());
    }

    public static final Pattern HASH_PATTERN = Pattern.compile("[0-9a-f]{7,40}", Pattern.CASE_INSENSITIVE);
}
