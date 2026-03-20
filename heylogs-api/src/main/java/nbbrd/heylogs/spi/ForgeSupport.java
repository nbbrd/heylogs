package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.DomainConfig;
import nbbrd.heylogs.ForgeConfig;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@lombok.Builder(toBuilder = true)
public final class ForgeSupport implements Forge {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Predicate<URL> knownHostPredicate;

    @lombok.Singular
    private final Map<ForgeLinkType, ForgeLinkParser> linkParsers;

    @lombok.Singular
    private final Map<ForgeLinkType, ForgeRefParser> refParsers;

    @lombok.Singular
    private final Map<ForgeLinkType, ForgeLinkResolver> linkResolvers;

    @lombok.Singular
    private final Map<ForgeLinkType, MessageFetcher> messageFetchers;

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
    public @Nullable CompareLinkParser getCompareLinkParser() {
        ForgeLinkParser result = getLinkParser(ForgeLinkType.COMPARE);
        return result != null ? CompareLinkParser.casting(result) : null;
    }

    @Override
    public @Nullable ForgeLinkParser getLinkParser(@NonNull ForgeLinkType type) {
        return linkParsers.get(type);
    }

    @Override
    public @Nullable ForgeRefParser getRefParser(@NonNull ForgeLinkType type) {
        return refParsers.get(type);
    }

    @Override
    public @Nullable ForgeLinkResolver getLinkResolver(@NonNull ForgeLinkType type) {
        return linkResolvers.get(type);
    }

    @Override
    public @Nullable MessageFetcher getMessageFetcher(@NonNull ForgeLinkType type) {
        return messageFetchers.get(type);
    }

    @Override
    public boolean isKnownHost(@NonNull URL url) {
        return knownHostPredicate.test(url);
    }

    public static final class Builder {

        public Builder parser(@NonNull ForgeLinkType type,
                              @NonNull ForgeLinkParser linkParser,
                              @NonNull ForgeRefParser refParser
        ) {
            return linkParser(type, linkParser).refParser(type, refParser);
        }
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Forge> onHost(@NonNull URL link, @NonNull List<DomainConfig> domains) {
        return forge -> forge.isKnownHost(link)
                || domains.stream().anyMatch(domain -> domain.isCompatibleWith(forge));
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Forge> onForgeConfig(@NonNull ForgeConfig config) {
        return other -> config.getId().equals(other.getForgeId());
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Forge> onDomainConfig(@NonNull DomainConfig config) {
        return config::isCompatibleWith;
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<URL> onHostContaining(@NonNull String domain) {
        return url -> Arrays.asList(url.getHost().split("\\.", -1)).contains(domain);
    }
}
