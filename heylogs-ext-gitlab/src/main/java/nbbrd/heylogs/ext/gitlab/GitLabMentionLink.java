package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.*;
import static java.util.Collections.unmodifiableList;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.NAMESPACE_PATTERN;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabMentionLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabMentionLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        int length = pathArray.length;

        if (length < 1) {
            throw new IllegalArgumentException("GitLab mention link must have at least 1 path segment");
        }

        for (int i = length - 1; i >= 0; i--) {
            checkPathItem(pathArray, i, NAMESPACE_PATTERN);
        }

        return new GitLabMentionLink(baseOf(url), unmodifiableList(Arrays.asList(pathArray)));
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(namespace).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }
}
