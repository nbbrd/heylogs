package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.baseOf;
import static internal.heylogs.spi.URLExtractor.getPathArray;
import static java.util.Collections.unmodifiableList;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.NAMESPACE_PATTERN;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.PROJECT_PATTERN;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabRepositoryLink implements GitLabProjectLink {

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @StaticFactoryMethod
    public static @NonNull GitLabRepositoryLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url, true);
        if (pathArray.length < 2)
            throw new IllegalArgumentException("GitLab project link must have at least 2 path segments");
        List<String> namespace = unmodifiableList(java.util.Arrays.asList(pathArray).subList(0, pathArray.length - 1));
        String project = pathArray[pathArray.length - 1];
        for (String ns : namespace) {
            if (!NAMESPACE_PATTERN.matcher(ns).matches())
                throw new IllegalArgumentException("Invalid namespace: " + ns);
        }
        if (!PROJECT_PATTERN.matcher(project).matches())
            throw new IllegalArgumentException("Invalid project: " + project);
        return new GitLabRepositoryLink(baseOf(url), namespace, project);
    }

    @Override
    public ForgeRef toRef(ForgeRef baseRef) {
        return null;
    }

    @Override
    public @NonNull URL toURL() {
        try {
            return URLQueryBuilder.of(base).path(namespace).path(project).build();
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return toURL().toString();
    }
}
