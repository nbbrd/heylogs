package internal.heylogs.github;

import internal.heylogs.GitHostLink;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.URLExtractor.*;
import static java.lang.Integer.parseInt;

// https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubIssueLink implements GitHostLink {

    public static final String ISSUES_TYPE = "issues";
    public static final String PULL_REQUEST_TYPE = "pull";

    @StaticFactoryMethod
    public static @NonNull GitHubIssueLink parse(@NonNull CharSequence text) {
        return parseURL(urlOf(text));
    }

    private static @NonNull GitHubIssueLink parseURL(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER);
        checkPathItem(pathArray, 1, REPO);
        checkPathItem(pathArray, 2, ISSUES_TYPE, PULL_REQUEST_TYPE);
        checkPathItem(pathArray, 3, NUMBER);

        return new GitHubIssueLink(baseOf(url), pathArray[0], pathArray[1], pathArray[2], parseInt(pathArray[3]));
    }

    @NonNull URL base;
    @NonNull String owner;
    @NonNull String repo;
    @NonNull String type;
    int issueNumber;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(type).path(String.valueOf(issueNumber)).toString();
    }

    private static final Pattern OWNER = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}");
    private static final Pattern REPO = Pattern.compile("[a-z\\d._-]{1,100}");
    private static final Pattern NUMBER = Pattern.compile("\\d+");
}
