package tests.heylogs.spi;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;

import java.net.URL;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockedForgeLink implements ForgeLink {

    @StaticFactoryMethod
    public static MockedForgeLink parse(@NonNull URL url) {
        return new MockedForgeLink(url);
    }

    @NonNull
    URL base;

    @Override
    public String toString() {
        return base.toString();
    }

    @Override
    public @NonNull URL toURL() {
        return base;
    }
}
