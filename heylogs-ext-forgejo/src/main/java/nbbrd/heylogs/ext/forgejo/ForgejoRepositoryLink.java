package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;

import java.net.URL;

import static nbbrd.heylogs.spi.URLExtractor.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoRepositoryLink implements ForgejoProjectLink {

    @StaticFactoryMethod
    public static @NonNull ForgejoRepositoryLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url, true);
        checkPathLength(pathArray, 2);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        return new ForgejoRepositoryLink(baseOf(url), pathArray[0], pathArray[1]);
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @Override
    public @NonNull URL toURL() {
        return getProjectURL();
    }

    @Override
    public ForgeRef toRef(ForgeRef baseRef) {
        return null;
    }

    @Override
    public String toString() {
        return toURL().toString();
    }
}
