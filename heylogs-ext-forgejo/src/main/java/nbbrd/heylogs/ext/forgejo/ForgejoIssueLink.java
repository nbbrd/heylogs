package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.*;
import static java.lang.Integer.parseInt;

// https://forgejo.org/docs/latest/user/linked-references/
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoIssueLink implements ForgeLink {

    public static final String ISSUES_TYPE = "issues";
    public static final String PULL_REQUEST_TYPE = "pulls";

    @StaticFactoryMethod
    public static @NonNull ForgejoIssueLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, ISSUES_TYPE, PULL_REQUEST_TYPE);
        checkPathItem(pathArray, 3, NUMBER_PATTERN);

        return new ForgejoIssueLink(baseOf(url), pathArray[0], pathArray[1], pathArray[2], parseInt(pathArray[3]));
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    String type;

    int issueNumber;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(type).path(String.valueOf(issueNumber)).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    private static final Pattern OWNER_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPO_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
}
