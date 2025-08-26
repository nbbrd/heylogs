package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.net.URL;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder(toBuilder = true)
public final class ForgeSupport implements Forge {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Function<URL, CompareLink> compareLinkFactory;

    private final @NonNull Predicate<ForgeLink> linkPredicate;

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
            return linkPredicate.test(compareLinkFactory.apply(url));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public @NonNull CompareLink getCompareLink(@NonNull URL url) {
        return compareLinkFactory.apply(url);
    }

    public static @NonNull Predicate<Forge> onForgeId(@NonNull String id) {
        return forge -> forge.getForgeId().equals(id);
    }

    public static @NonNull Predicate<Forge> onCompareLink(@NonNull URL link) {
        return forge -> forge.isCompareLink(link);
    }
}
