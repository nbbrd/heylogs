package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.*;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabMergeRequestLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabMergeRequestLink parse(@NonNull CharSequence text) {
        return parseLink(
                (base, namespace, project, value) -> new GitLabMergeRequestLink(base, namespace, project, Integer.parseInt(value)),
                MERGE_REQUEST_KEYWORD, NUMBER_PATTERN, urlOf(text)
        );
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

    private static final String MERGE_REQUEST_KEYWORD = "merge_requests";
}
