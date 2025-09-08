package nbbrd.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.spi.*;

import java.util.Comparator;

import static java.util.Comparator.comparing;

@lombok.Value
@lombok.Builder
public class Resource {

    @NonNull
    String type;
    @NonNull
    String module;
    @NonNull
    String id;
    @NonNull
    String name;
    @NonNull
    String options;

    static Resource of(Rule rule) {
        return Resource
                .builder()
                .type("rule")
                .module(rule.getRuleModuleId())
                .id(rule.getRuleId())
                .name(rule.getRuleName())
                .options(rule.getRuleSeverity().name())
                .build();
    }

    static Resource of(Format format) {
        return Resource
                .builder()
                .type("format")
                .module(format.getFormatModuleId())
                .id(format.getFormatId())
                .name(format.getFormatName())
                .options("")
                .build();
    }

    static Resource of(Versioning versioning) {
        return Resource
                .builder()
                .type("versioning")
                .module(versioning.getVersioningModuleId())
                .id(versioning.getVersioningId())
                .name(versioning.getVersioningName())
                .options("")
                .build();
    }

    static Resource of(Forge forge) {
        return Resource
                .builder()
                .type("forge")
                .module(forge.getForgeModuleId())
                .id(forge.getForgeId())
                .name(forge.getForgeName())
                .options("")
                .build();
    }

    static Resource of(Tagging tagging) {
        return Resource
                .builder()
                .type("tagging")
                .module(tagging.getTaggingModuleId())
                .id(tagging.getTaggingId())
                .name(tagging.getTaggingName())
                .options("")
                .build();
    }

    static final Comparator<Resource> DEFAULT_COMPARATOR
            = comparing(Resource::getType)
            .thenComparing(Resource::getModule)
            .thenComparing(Resource::getId);
}
