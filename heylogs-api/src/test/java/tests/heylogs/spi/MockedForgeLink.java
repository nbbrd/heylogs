package tests.heylogs.spi;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;

import java.net.URL;

import static internal.heylogs.spi.URLExtractor.urlOf;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockedForgeLink implements ForgeLink {

    @StaticFactoryMethod
    public static MockedForgeLink parse(@NonNull CharSequence text) {
        return new MockedForgeLink(urlOf(text));
    }

    @NonNull
    URL base;

    @Override
    public String toString() {
        return base.toString();
    }
}
