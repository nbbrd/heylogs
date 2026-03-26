package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.net.URL;

@RepresentableAs(URL.class)
@lombok.Value(staticConstructor = "of")
public class MockedForgeLink implements ForgeLink {

    @StaticFactoryMethod
    public static MockedForgeLink parse(@NonNull URL url) {
        return new MockedForgeLink(url, null);
    }

    @NonNull
    URL url;

    @Nullable
    MockedForgeRef ref;

    @Override
    public String toString() {
        return url.toString();
    }

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
        return ref;
    }
}
