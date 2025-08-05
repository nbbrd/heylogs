package nbbrd.heylogs.ext.github;

import internal.heylogs.git.Hash;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.git.Hash.HASH_PATTERN;
import static internal.heylogs.spi.URLExtractor.*;

// https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#commit-shas
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubCommitLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitHubCommitLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, COMMIT_KEYWORD);
        checkPathItem(pathArray, 3, HASH_PATTERN);

        return new GitHubCommitLink(baseOf(url), pathArray[0], pathArray[1], Hash.parse(pathArray[3]));
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    Hash hash;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(COMMIT_KEYWORD).path(hash.toString()).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    private static final Pattern OWNER_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPO_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    private static final String COMMIT_KEYWORD = "commit";
}
