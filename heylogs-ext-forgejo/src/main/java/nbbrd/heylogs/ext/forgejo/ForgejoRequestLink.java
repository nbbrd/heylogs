package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.*;
import static java.lang.Integer.parseInt;

// https://forgejo.org/docs/latest/user/linked-references/
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoRequestLink implements ForgeLink {

    private static final String PULL_REQUEST_KEYWORD = "pulls";

    @StaticFactoryMethod
    public static @NonNull ForgejoRequestLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, PULL_REQUEST_KEYWORD);
        checkPathItem(pathArray, 3, NUMBER_PATTERN);

        return new ForgejoRequestLink(baseOf(url), pathArray[0], pathArray[1], parseInt(pathArray[3]));
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    int issueNumber;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(PULL_REQUEST_KEYWORD).path(String.valueOf(issueNumber)).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @NonNull ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return ForgejoRequestRef.of(this, baseRef instanceof ForgejoRequestRef ? (ForgejoRequestRef) baseRef : null);
    }

    private static final Pattern OWNER_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPO_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
}
