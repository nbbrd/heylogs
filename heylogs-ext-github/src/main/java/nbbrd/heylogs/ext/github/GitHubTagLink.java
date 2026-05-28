package nbbrd.heylogs.ext.github;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.URLExtractor.*;

// https://docs.github.com/en/repositories/releasing-projects-on-github/viewing-your-repositorys-releases-and-tags
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubTagLink implements GitHubProjectLink {

    @StaticFactoryMethod
    public static @NonNull GitHubTagLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 5);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, RELEASES_KEYWORD);
        checkPathItem(pathArray, 3, TAG_KEYWORD);
        checkPathItem(pathArray, 4, TAG_PATTERN);

        return new GitHubTagLink(baseOf(url), pathArray[0], pathArray[1], pathArray[4]);
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    String tag;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(RELEASES_KEYWORD).path(TAG_KEYWORD).path(tag).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    private static final String RELEASES_KEYWORD = "releases";
    private static final String TAG_KEYWORD = "tag";
    static final Pattern TAG_PATTERN = Pattern.compile("[a-z\\d][a-z\\d._+\\-]*", Pattern.CASE_INSENSITIVE);
}

