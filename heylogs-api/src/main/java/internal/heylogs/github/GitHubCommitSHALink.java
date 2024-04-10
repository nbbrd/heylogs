package internal.heylogs.github;

import nbbrd.heylogs.spi.ForgeLink;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.URLExtractor.*;

// https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#commit-shas
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubCommitSHALink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitHubCommitSHALink parse(@NonNull CharSequence text) {
        return parseURL(urlOf(text));
    }

    private static @NonNull GitHubCommitSHALink parseURL(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER);
        checkPathItem(pathArray, 1, REPO);
        checkPathItem(pathArray, 2, "commit");
        checkPathItem(pathArray, 3, HASH);

        return new GitHubCommitSHALink(baseOf(url), pathArray[0], pathArray[1], pathArray[3]);
    }

    @NonNull URL base;
    @NonNull String owner;
    @NonNull String repo;
    @NonNull String hash;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path("commit").path(hash).toString();
    }

    private static final Pattern OWNER = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}");
    private static final Pattern REPO = Pattern.compile("[a-z\\d._-]{1,100}");
    private static final Pattern HASH = Pattern.compile("[0-9a-f]{40}");
}
