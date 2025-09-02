package nbbrd.heylogs;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLoader;
import org.jspecify.annotations.NonNull;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ForgeConfig {

    @StaticFactoryMethod
    public static @NonNull ForgeConfig parse(@NonNull CharSequence text) {
        return of(text.toString());
    }

    @StaticFactoryMethod
    public static @NonNull ForgeConfig of(@NonNull String id) {
        if (!ForgeLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID '" + id + "'");
        }
        return new ForgeConfig(id);
    }

    @NonNull
    String id;

    @Override
    public String toString() {
        return id;
    }
}
