package nbbrd.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.Versioning;

import java.util.Comparator;

import static java.util.Comparator.comparing;

@lombok.Value
@lombok.Builder
public class Resource {

    @NonNull
    String type;
    @NonNull
    String category;
    @NonNull
    String id;
    @NonNull
    String name;

    static Resource of(Rule rule) {
        return Resource
                .builder()
                .type("rule")
                .category(rule.getRuleCategory())
                .id(rule.getRuleId())
                .name(rule.getRuleName())
                .build();
    }

    static Resource of(Format format) {
        return Resource
                .builder()
                .type("format")
                .category(format.getFormatCategory())
                .id(format.getFormatId())
                .name(format.getFormatName())
                .build();
    }

    static Resource of(Versioning versioning) {
        return Resource
                .builder()
                .type("versioning")
                .category("main")
                .id(versioning.getVersioningId())
                .name(versioning.getVersioningName())
                .build();
    }

    static Resource of(Forge forge) {
        return Resource
                .builder()
                .type("forge")
                .category("main")
                .id(forge.getForgeId())
                .name(forge.getForgeName())
                .build();
    }

    static final Comparator<Resource> DEFAULT_COMPARATOR
            = comparing(Resource::getType)
            .thenComparing(Resource::getCategory)
            .thenComparing(Resource::getId);
}
