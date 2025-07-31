package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.*;

// https://forgejo.org/docs/latest/user/linked-references/
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoMentionLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull ForgejoMentionLink parse(@NonNull CharSequence text) {
        return parseURL(urlOf(text));
    }

    private static @NonNull ForgejoMentionLink parseURL(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 1, 4);

        if (pathArray.length == 1) {
            checkPathItem(pathArray, 0, USER);
            return new ForgejoMentionLink(baseOf(url), pathArray[0], null, null);
        }

        checkPathItem(pathArray, 0, "orgs");
        checkPathItem(pathArray, 1, ORGANIZATION);
        checkPathItem(pathArray, 2, "teams");
        checkPathItem(pathArray, 3, TEAM);
        return new ForgejoMentionLink(baseOf(url), null, pathArray[1], pathArray[3]);
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

    private static final Pattern USER = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORGANIZATION = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEAM = Pattern.compile("[^:/$]+", Pattern.CASE_INSENSITIVE);
}
