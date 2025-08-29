package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder(toBuilder = true)
public final class ForgeSupport implements Forge {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Function<URL, CompareLink> compareLinkFactory;

    private final @NonNull Predicate<URL> knownHostPredicate;

    @lombok.Singular
    private final Map<ForgeRefType, Function<? super URL, ForgeLink>> linkParsers;

    @lombok.Singular
    private final Map<ForgeRefType, Function<? super CharSequence, ForgeRef>> refParsers;

    @Override
    public @NonNull String getForgeId() {
        return id;
    }

    @Override
    public @NonNull String getForgeName() {
        return name;
    }

    @Override
    public @NonNull String getForgeModuleId() {
        return moduleId;
    }

    @Override
    public boolean isCompareLink(@NonNull URL url) {
        try {
            return knownHostPredicate.test(url) && compareLinkFactory.apply(url) != null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public @NonNull CompareLink getCompareLink(@NonNull URL url) {
        return compareLinkFactory.apply(url);
    }

    @Override
    public @Nullable Function<? super URL, ForgeLink> getLinkParser(@NonNull ForgeRefType type) {
        return linkParsers.get(type);
    }

    @Override
    public @Nullable Function<? super CharSequence, ForgeRef> getRefParser(@NonNull ForgeRefType type) {
        return refParsers.get(type);
    }

    @Override
    public boolean isKnownHost(@NonNull URL url) {
        return knownHostPredicate.test(url);
    }

    public static final class Builder {

        public Builder parser(@NonNull ForgeRefType type,
                              @NonNull Function<? super URL, ForgeLink> linkParser,
                              @NonNull Function<? super CharSequence, ForgeRef> refParser
        ) {
            return linkParser(type, linkParser).refParser(type, refParser);
        }
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Forge> onForgeId(@NonNull String id) {
        return forge -> forge.getForgeId().equals(id);
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Forge> onCompareLink(@NonNull URL link) {
        return forge -> forge.isCompareLink(link);
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<URL> onHostContaining(@NonNull String domain) {
        return url -> Arrays.asList(url.getHost().split("\\.", -1)).contains(domain);
    }
}
