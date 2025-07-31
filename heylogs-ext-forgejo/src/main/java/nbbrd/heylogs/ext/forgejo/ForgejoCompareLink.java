package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.CompareLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.*;

@RepresentableAsString
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoCompareLink implements CompareLink {

    @StaticFactoryMethod
    public static @NonNull ForgejoCompareLink parse(@NonNull CharSequence text) {
        return parse(urlOf(text));
    }

    @StaticFactoryMethod
    public static @NonNull ForgejoCompareLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        checkPathLength(pathArray, 4);
        checkPathItem(pathArray, 0, OWNER);
        checkPathItem(pathArray, 1, REPO);
        checkPathItem(pathArray, 2, "compare");
        checkPathItem(pathArray, 3, OID);

        return new ForgejoCompareLink(baseOf(url), pathArray[0], pathArray[1], pathArray[2], pathArray[3]);
    }

    @NonNull
    URL base;
    @NonNull
    String owner;
    @NonNull
    String repo;
    @NonNull
    String type;
    @NonNull
    String oid;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(type).path(oid).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @NonNull ForgejoCompareLink derive(@NonNull String tag) {
        return new ForgejoCompareLink(base, owner, repo, type, getOid(tag));
    }

    @Override
    public @NonNull URL getProjectURL() {
        return urlOf(URLQueryBuilder.of(base).path(owner).path(repo).toString());
    }

    private String getOid(String tag) {
        return oid.endsWith("...HEAD")
                ? oid.startsWith("HEAD...") ? (tag + "..." + tag) : (oid.substring(0, oid.length() - 4) + tag)
                : (oid.substring(oid.indexOf("...") + 3) + "..." + tag);
    }

    private static final Pattern OWNER = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPO = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    private static final Pattern OID = Pattern.compile(".+\\.{3}.+");
}
