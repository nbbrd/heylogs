package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.net.URL;

public interface CompareLink extends ProjectLink {

    @NonNull
    CompareLink derive(@NonNull String tag);

    @NonNull
    String getCompareBaseRef();

    @NonNull
    String getCompareHeadRef();
}
