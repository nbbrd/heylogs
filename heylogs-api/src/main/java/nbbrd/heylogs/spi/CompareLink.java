package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.net.URL;

public interface CompareLink extends ForgeLink {

    @NonNull
    CompareLink derive(@NonNull String tag);

    @NonNull
    URL getProjectURL();

}
