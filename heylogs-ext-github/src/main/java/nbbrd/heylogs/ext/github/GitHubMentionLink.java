package nbbrd.heylogs.ext.github;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.*;

// https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#mentioning-people-and-teams
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubMentionLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitHubMentionLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 1, 4);

        if (pathArray.length == 1) {
            checkPathItem(pathArray, 0, USER_PATTERN);
            return new GitHubMentionLink(baseOf(url), pathArray[0], null, null);
        }

        checkPathItem(pathArray, 0, ORGS_KEYWORD);
        checkPathItem(pathArray, 1, ORGANIZATION_PATTERN);
        checkPathItem(pathArray, 2, TEAMS_KEYWORD);
        checkPathItem(pathArray, 3, TEAM_PATTERN);
        return new GitHubMentionLink(baseOf(url), null, pathArray[1], pathArray[3]);
    }

    @NonNull
    URL base;

    @Nullable
    String user;

    @Nullable
    String organization;

    @Nullable
    String teamName;

    @SuppressWarnings("DataFlowIssue")
    @Override
    public String toString() {
        return isUser()
                ? URLQueryBuilder.of(base).path(user).toString()
                : URLQueryBuilder.of(base).path(ORGS_KEYWORD).path(organization).path(TEAMS_KEYWORD).path(teamName).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    public boolean isUser() {
        return user != null;
    }

    private static final Pattern USER_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORGANIZATION_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEAM_PATTERN = Pattern.compile("[^:/$]+", Pattern.CASE_INSENSITIVE);
    private static final String ORGS_KEYWORD = "orgs";
    private static final String TEAMS_KEYWORD = "teams";
}
