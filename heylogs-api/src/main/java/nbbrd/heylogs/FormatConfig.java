package nbbrd.heylogs;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLoader;
import nbbrd.heylogs.spi.FormatLoader;
import org.jspecify.annotations.NonNull;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FormatConfig {

    @StaticFactoryMethod
    public static @NonNull FormatConfig parse(@NonNull CharSequence text) {
        return of(text.toString());
    }

    @StaticFactoryMethod
    public static @NonNull FormatConfig of(@NonNull String id) {
        if (!FormatLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID '" + id + "'");
        }
        return new FormatConfig(id);
    }

    @NonNull
    String id;

    @Override
    public String toString() {
        return id;
    }
}
