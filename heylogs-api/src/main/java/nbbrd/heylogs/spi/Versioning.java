package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batchType = VersioningBatch.class
)
public interface Versioning {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull
    String getVersioningId();

    @NonNull
    String getVersioningName();

    @NonNull
    URL getVersioningUrl();

    @NonNull
    String getVersioningModuleId();

    @NonNull
    Validator<String> getVersioningArgValidator();

    /**
     * Returns a predicate to validate version strings, or null if not supported.
     */
    @Nullable
    Predicate<CharSequence> getVersioningPredicateOrNull(@Nullable String arg) throws IllegalArgumentException;

    /**
     * Returns a comparator to order versions, or null if not supported.
     * Used to detect version misordering.
     */
    @Nullable
    Comparator<CharSequence> getVersioningComparatorOrNull(@Nullable String arg) throws IllegalArgumentException;

    /**
     * Returns a function mapping versions to family keys, or null if not supported.
     * Versions with the same family key (e.g., "2.4" for SemVer) should maintain strict ordering.
     * This distinguishes typos from legitimate hotfixes across branches.
     */
    @Nullable
    Function<CharSequence, String> getVersioningFamilyMapperOrNull(@Nullable String arg) throws IllegalArgumentException;

    Predicate<CharSequence> NO_VERSIONING_FILTER = null;
    Comparator<CharSequence> NO_VERSIONING_COMPARATOR = null;
    Function<CharSequence, String> NO_VERSIONING_FAMILY_MAPPER = null;
}
