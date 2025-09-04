package nbbrd.heylogs;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningLoader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VersioningConfig {

    @StaticFactoryMethod
    public static @NonNull VersioningConfig parse(@NonNull CharSequence text) {
        String textString = text.toString();
        int index = textString.indexOf(':');
        return index != -1
                ? of(textString.substring(0, index), textString.substring(index + 1))
                : of(textString, null);
    }

    @StaticFactoryMethod
    public static @NonNull VersioningConfig of(@NonNull String id) {
        if (!VersioningLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID '" + id + "'");
        }
        return new VersioningConfig(id, null);
    }

    @StaticFactoryMethod
    public static @NonNull VersioningConfig of(@NonNull String id, @Nullable String arg) {
        if (!VersioningLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID '" + id + "'");
        }
        return new VersioningConfig(id, arg);
    }

    @NonNull
    String id;

    @Nullable
    String arg;

    @Override
    public String toString() {
        return id + (arg != null ? (":" + arg) : "");
    }

    public boolean isCompatibleWith(@NonNull Versioning other) {
        return this.id.equals(other.getVersioningId());
    }
}
