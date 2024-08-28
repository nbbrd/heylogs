package nbbrd.heylogs.ext.github;

import nbbrd.heylogs.spi.ForgeLink;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.URLQueryBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.URLExtractor.*;

// https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#mentioning-people-and-teams
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubMentionLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitHubMentionLink parse(@NonNull CharSequence text) {
        return parseURL(urlOf(text));
    }

    private static @NonNull GitHubMentionLink parseURL(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 1, 4);

        if (pathArray.length == 1) {
            checkPathItem(pathArray, 0, USER);
            return new GitHubMentionLink(baseOf(url), pathArray[0], null, null);
        }

        checkPathItem(pathArray, 0, "orgs");
        checkPathItem(pathArray, 1, ORGANIZATION);
        checkPathItem(pathArray, 2, "teams");
        checkPathItem(pathArray, 3, TEAM);
        return new GitHubMentionLink(baseOf(url), null, pathArray[1], pathArray[3]);
    }

    @NonNull URL base;
    @Nullable String user;
    @Nullable String organization;
    @Nullable String teamName;

    @SuppressWarnings("DataFlowIssue")
    @Override
    public String toString() {
        return isUser()
                ? URLQueryBuilder.of(base).path(user).toString()
                : URLQueryBuilder.of(base).path("orgs").path(organization).path("teams").path(teamName).toString();
    }

    public boolean isUser() {
        return user != null;
    }

    private static final Pattern USER = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}");
    private static final Pattern ORGANIZATION = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}");
    private static final Pattern TEAM = Pattern.compile("[^:/$]+");
}
