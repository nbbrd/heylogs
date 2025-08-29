package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabIssueLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabIssueLink parse(@NonNull URL url) {
        return parseLink(GitLabIssueLink::new, ISSUES_KEYWORD, NUMBER_PATTERN, Integer::parseInt, url);
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    int number;

    @Override
    public String toString() {
        return linkToString(base, namespace, project, ISSUES_KEYWORD, String.valueOf(number));
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @NonNull ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return GitLabIssueRef.of(this, baseRef instanceof GitLabIssueRef ? (GitLabIssueRef) baseRef : null);
    }

    private static final String ISSUES_KEYWORD = "issues";
}
