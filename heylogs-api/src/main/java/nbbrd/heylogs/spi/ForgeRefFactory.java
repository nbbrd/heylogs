package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ForgeRefFactory<L extends ForgeLink, R extends ForgeRef<L>> {

    @NonNull
    L parseLink(@NonNull URL text) throws IllegalArgumentException;

    @NonNull
    R parseRef(@NonNull CharSequence text) throws IllegalArgumentException;

    @NonNull
    R toRef(@NonNull L link, @Nullable R baseRef);

    static <L extends ForgeLink, R extends ForgeRef<L>> @NonNull ForgeRefFactory<L, R> of(
            @NonNull Function<? super URL, L> linkParser,
            @NonNull Function<? super CharSequence, R> refParser,
            @NonNull BiFunction<@NonNull L, @Nullable R, @NonNull R> expectation
    ) {
        return new ForgeRefFactory<L, R>() {
            @Override
            public @NonNull L parseLink(@NonNull URL text) {
                return linkParser.apply(text);
            }

            @Override
            public @NonNull R parseRef(@NonNull CharSequence text) {
                return refParser.apply(text);
            }

            @Override
            public @NonNull R toRef(@NonNull L link, @Nullable R baseRef) {
                return expectation.apply(link, baseRef);
            }
        };
    }
}
