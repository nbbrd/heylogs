package nbbrd.heylogs.ext.github;

import internal.heylogs.git.ThreeDotDiff;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.CompareLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.heylogs.spi.ProjectLink;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;

import static internal.heylogs.git.ThreeDotDiff.THREE_DOT_DIFF_PATTERN;
import static internal.heylogs.spi.URLExtractor.*;

// https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-comparing-branches-in-pull-requests#three-dot-and-two-dot-git-diff-comparisons
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubCompareLink implements CompareLink, GitHubProjectLink {

    @StaticFactoryMethod
    public static @NonNull GitHubCompareLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, COMPARE_KEYWORD);
        checkPathItem(pathArray, 3, THREE_DOT_DIFF_PATTERN);

        return new GitHubCompareLink(baseOf(url), pathArray[0], pathArray[1], ThreeDotDiff.parse(pathArray[3]));
    }

    @StaticFactoryMethod
    public static @NonNull GitHubCompareLink of(@NonNull ProjectLink link) {
        if (link instanceof GitHubCompareLink) return (GitHubCompareLink) link;
        if (link instanceof GitHubProjectLink) {
            GitHubProjectLink github = (GitHubProjectLink) link;
            return new GitHubCompareLink(github.getBase(), github.getOwner(), github.getRepo(), ThreeDotDiff.DEFAULT);
        }
        throw new IllegalArgumentException("Cannot create compare link from non-GitHub project link: " + link);
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    ThreeDotDiff diff;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(COMPARE_KEYWORD).path(diff.toString()).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    @Override
    public @NonNull GitHubCompareLink derive(@NonNull String tag) {
        return new GitHubCompareLink(base, owner, repo, diff.derive(tag));
    }

    @Override
    public @NonNull String getCompareBaseRef() {
        return diff.getFrom();
    }

    @Override
    public @NonNull String getCompareHeadRef() {
        return diff.getTo();
    }

    private static final String COMPARE_KEYWORD = "compare";
}
