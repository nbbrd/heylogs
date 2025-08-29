package nbbrd.heylogs.ext.forgejo;

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
import java.util.regex.Pattern;

import static internal.heylogs.git.ThreeDotDiff.THREE_DOT_DIFF_PATTERN;
import static internal.heylogs.spi.URLExtractor.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoCompareLink implements CompareLink {

    @StaticFactoryMethod
    public static @NonNull ForgejoCompareLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, COMPARE_KEYWORD);
        checkPathItem(pathArray, 3, THREE_DOT_DIFF_PATTERN);

        return new ForgejoCompareLink(baseOf(url), pathArray[0], pathArray[1], ThreeDotDiff.parse(pathArray[3]));
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    ThreeDotDiff diff;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(COMPARE_KEYWORD).path(diff.toString()).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    @Override
    public @NonNull ForgejoCompareLink derive(@NonNull String tag) {
        return new ForgejoCompareLink(base, owner, repo, diff.derive(tag));
    }

    @Override
    public @NonNull URL getProjectURL() {
        return urlOf(URLQueryBuilder.of(base).path(owner).path(repo).toString());
    }

    private static final Pattern OWNER_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPO_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    private static final String COMPARE_KEYWORD = "compare";
}
