package nbbrd.heylogs.ext.github;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;

import static internal.heylogs.spi.URLExtractor.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubRepositoryLink implements GitHubProjectLink {

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @StaticFactoryMethod
    public static @NonNull GitHubRepositoryLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url, true);
        checkPathLength(pathArray, 2);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        return new GitHubRepositoryLink(baseOf(url), pathArray[0], pathArray[1]);
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(URLQueryBuilder.of(base).path(owner).path(repo).toString());
    }

    @Override
    public @NonNull URL getProjectURL() {
        return toURL();
    }

    @Override
    public String toString() {
        return toURL().toString();
    }

    @Override
    public ForgeRef toRef(ForgeRef baseRef) {
        return null;
    }
}
