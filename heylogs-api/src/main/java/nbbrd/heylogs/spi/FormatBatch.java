package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.util.stream.Stream;

public interface FormatBatch {

    @NonNull Stream<Format> getProviders();
}
