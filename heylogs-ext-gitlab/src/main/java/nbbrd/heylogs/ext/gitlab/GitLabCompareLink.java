package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.CompareLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.OID_PATTERN;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.parseLink;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCompareLink implements CompareLink {

    @StaticFactoryMethod
    public static @NonNull GitLabCompareLink parse(@NonNull URL url) {
        return parseLink(
                GitLabCompareLink::new,
                COMPARE_KEYWORD, OID_PATTERN, url
        );
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @NonNull
    String oid;

    @Override
    public String toString() {
        return toURL().toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(GitLabSupport.linkToString(base, namespace, project, "compare", oid));
    }

    @Override
    public @NonNull GitLabCompareLink derive(@NonNull String tag) {
        return new GitLabCompareLink(base, namespace, project, getOid(tag));
    }

    @Override
    public @NonNull URL getProjectURL() {
        return urlOf(URLQueryBuilder.of(base).path(namespace).path(project).toString());
    }

    private String getOid(String tag) {
        return oid.endsWith("...HEAD")
                ? oid.startsWith("HEAD...") ? (tag + "..." + tag) : (oid.substring(0, oid.length() - 4) + tag)
                : (oid.substring(oid.indexOf("...") + 3) + "..." + tag);
    }

    private static final String COMPARE_KEYWORD = "compare";
}
