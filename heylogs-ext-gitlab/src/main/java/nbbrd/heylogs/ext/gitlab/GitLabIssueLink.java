package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.*;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabIssueLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabIssueLink parse(@NonNull CharSequence text) {
        return parseLink(
                (base, namespace, project, value) -> new GitLabIssueLink(base, namespace, project, Integer.parseInt(value)),
                ISSUES_KEYWORD, NUMBER_PATTERN, urlOf(text)
        );
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

    private static final String ISSUES_KEYWORD = "issues";
}
