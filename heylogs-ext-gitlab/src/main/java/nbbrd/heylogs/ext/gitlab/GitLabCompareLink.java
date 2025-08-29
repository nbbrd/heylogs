package nbbrd.heylogs.ext.gitlab;

import internal.heylogs.git.ThreeDotDiff;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.CompareLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;

import static internal.heylogs.git.ThreeDotDiff.THREE_DOT_DIFF_PATTERN;
import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.parseLink;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCompareLink implements CompareLink {

    @StaticFactoryMethod
    public static @NonNull GitLabCompareLink parse(@NonNull URL url) {
        return parseLink(GitLabCompareLink::new, COMPARE_KEYWORD, THREE_DOT_DIFF_PATTERN, ThreeDotDiff::parse, url);
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
    public @NonNull URL getProjectURL() {
        return urlOf(URLQueryBuilder.of(base).path(namespace).path(project).toString());
    }

    private static final String COMPARE_KEYWORD = "compare";
}
