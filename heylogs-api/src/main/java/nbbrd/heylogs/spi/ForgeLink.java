package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.net.URL;

public interface ForgeLink {

    @NonNull
    URL toURL();
}
