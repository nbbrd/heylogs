package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.*;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.spi.Tagging.CONVERSION_NOT_SUPPORTED;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_COMPARATOR;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FAMILY_MAPPER;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FILTER;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class RuleContext {

    public static final RuleContext DEFAULT = RuleContext.builder().build();

    @NonNull
    @lombok.With
    @lombok.Builder.Default
    Config config = Config.DEFAULT;

    @lombok.Singular
    List<Forge> forges;

    @lombok.Singular
    List<Versioning> versionings;

    @lombok.Singular
    List<Tagging> taggings;

    public @Nullable Predicate<CharSequence> findVersioningPredicateOrNull() {
        VersioningConfig versioningConfig = config.getVersioning();
        return versioningConfig != null ? versionings.stream()
                .filter(versioningConfig::isCompatibleWith)
                .map(v -> v.getVersioningPredicateOrNull(versioningConfig.getArg()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(NO_VERSIONING_FILTER) : NO_VERSIONING_FILTER;
    }

    public @Nullable Comparator<CharSequence> findVersioningComparatorOrNull() {
        VersioningConfig versioningConfig = config.getVersioning();
        return versioningConfig != null ? versionings.stream()
                .filter(versioningConfig::isCompatibleWith)
                .map(v -> v.getVersioningComparatorOrNull(versioningConfig.getArg()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(NO_VERSIONING_COMPARATOR) : NO_VERSIONING_COMPARATOR;
    }

    public @Nullable Function<CharSequence, String> findVersioningFamilyMapperOrNull() {
        VersioningConfig versioningConfig = config.getVersioning();
        return versioningConfig != null ? versionings.stream()
                .filter(versioningConfig::isCompatibleWith)
                .map(v -> v.getVersioningFamilyMapperOrNull(versioningConfig.getArg()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(NO_VERSIONING_FAMILY_MAPPER) : NO_VERSIONING_FAMILY_MAPPER;
    }

    public @Nullable RuleSeverity findRuleSeverityOrNull(@NonNull String ruleId) {
        return config.getRules()
                .stream()
                .filter(ruleConfig -> ruleConfig.getId().equals(ruleId))
                .findFirst()
                .map(RuleConfig::getSeverity)
                .orElse(null);
    }

    public @Nullable Converter<String, String> findTagParserOrNull() {
        TaggingConfig taggingConfig = config.getTagging();
        return taggingConfig != null ? taggings.stream()
                .filter(taggingConfig::isCompatibleWith)
                .map(o -> o.getTagParserOrNull(taggingConfig.getArg()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CONVERSION_NOT_SUPPORTED) : CONVERSION_NOT_SUPPORTED;
    }

    public @NonNull List<Forge> findAllForges(@NonNull URL url) {
        DomainConfig domain = getDomainByUrl(url);
        return forges.stream()
                .filter(forge -> forge.isKnownHost(url) || isCompatible(forge, domain))
                .collect(toList());
    }

    private DomainConfig getDomainByUrl(URL url) {
        return config.getDomains().stream()
                .filter(domainConfig -> url.getHost().contains(domainConfig.getDomain()))
                .findFirst()
                .orElse(null);
    }

    private static boolean isCompatible(Forge forge, DomainConfig config) {
        return config != null && config.isCompatibleWith(forge);
    }
}
