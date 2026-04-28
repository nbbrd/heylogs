package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.heylogs.spi.ProjectLink;
import org.jspecify.annotations.Nullable;

import java.net.URL;

@lombok.Value
public class MockedRepositoryLink implements ProjectLink {

    public static MockedRepositoryLink parse(@NonNull URL url) {
        if (url.getPath().contains("/compare"))
            throw new IllegalArgumentException("Not a repository link: " + url);
        return new MockedRepositoryLink(url);
    }

    URL url;

    @Override
    public @NonNull URL getBase() {
        return toURL();
    }

    @Override
    public @NonNull URL toURL() {
        return url;
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    @Override
    public @NonNull URL getProjectURL() {
        return url;
    }
}
