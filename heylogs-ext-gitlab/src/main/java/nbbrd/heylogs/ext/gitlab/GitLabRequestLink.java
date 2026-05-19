package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabRequestLink implements GitLabProjectLink {

    @StaticFactoryMethod
    public static @NonNull GitLabRequestLink parse(@NonNull URL url) {
        return parseLink(GitLabRequestLink::new, MERGE_REQUEST_KEYWORD, NUMBER_PATTERN, Integer::parseInt, url);
    }

    @StaticFactoryMethod
    public static @NonNull GitLabRequestLink resolve(@NonNull URL projectUrl, @NonNull CharSequence ref) {
        try {
            return parse(
                    URLQueryBuilder.of(projectUrl)
                            .path(MERGE_REQUEST_KEYWORD)
                            .path(String.valueOf(GitLabRequestRef.parse(ref).getNumber()))
                            .build());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
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
        return linkToString(base, namespace, project, MERGE_REQUEST_KEYWORD, String.valueOf(number));
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @NonNull ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return GitLabRequestRef.of(this, baseRef instanceof GitLabRequestRef ? (GitLabRequestRef) baseRef : null);
    }

    private static final String MERGE_REQUEST_KEYWORD = "merge_requests";
}
