package nbbrd.heylogs.spi;

import lombok.NonNull;
import java.net.URL;

public interface ProjectLink extends ForgeLink {

    @NonNull
    URL getProjectURL();
}
