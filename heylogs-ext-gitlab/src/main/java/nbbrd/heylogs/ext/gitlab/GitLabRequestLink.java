package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabRequestLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabRequestLink parse(@NonNull URL url) {
        return parseLink(GitLabRequestLink::new, MERGE_REQUEST_KEYWORD, NUMBER_PATTERN, Integer::parseInt, url);
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

    private static final String MERGE_REQUEST_KEYWORD = "merge_requests";
}
