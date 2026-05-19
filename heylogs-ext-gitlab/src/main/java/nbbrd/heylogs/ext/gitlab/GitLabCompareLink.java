package nbbrd.heylogs.ext.gitlab;

import nbbrd.heylogs.spi.ThreeDotDiff;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.CompareLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.heylogs.spi.ProjectLink;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;

import static nbbrd.heylogs.spi.ThreeDotDiff.THREE_DOT_DIFF_PATTERN;
import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.parseLink;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCompareLink implements CompareLink, GitLabProjectLink {

    @StaticFactoryMethod
    public static @NonNull GitLabCompareLink parse(@NonNull URL url) {
        return parseLink(GitLabCompareLink::new, COMPARE_KEYWORD, THREE_DOT_DIFF_PATTERN, ThreeDotDiff::parse, url);
    }

    @StaticFactoryMethod
    public static @NonNull GitLabCompareLink of(@NonNull ProjectLink link) {
        if (link instanceof GitLabCompareLink) return (GitLabCompareLink) link;
        if (link instanceof GitLabProjectLink) {
            GitLabProjectLink gitLab = (GitLabProjectLink) link;
            return new GitLabCompareLink(gitLab.getBase(), gitLab.getNamespace(), gitLab.getProject(), ThreeDotDiff.DEFAULT);
        }
        throw new IllegalArgumentException("Cannot create compare link from non-GitLab project link: " + link);
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @NonNull
    ThreeDotDiff diff;

    @Override
    public String toString() {
        return toURL().toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(GitLabSupport.linkToString(base, namespace, project, COMPARE_KEYWORD, diff.toString()));
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    @Override
    public @NonNull GitLabCompareLink derive(@NonNull String tag) {
        return new GitLabCompareLink(base, namespace, project, diff.derive(tag));
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
