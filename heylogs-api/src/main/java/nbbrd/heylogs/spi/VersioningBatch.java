package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.util.stream.Stream;

public interface VersioningBatch {

    @NonNull Stream<Versioning> getProviders();
}
