package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.List;

import static internal.heylogs.spi.URLExtractor.*;
import static nbbrd.heylogs.ext.gitlab.GitLab.*;

// https://docs.gitlab.com/user/markdown/#gitlab-specific-references
// https://docs.gitlab.com/user/reserved_names/#limitations-on-usernames-project-and-group-names
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCommitLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabCommitLink parse(@NonNull CharSequence text) {
        return parseURL(urlOf(text));
    }

    private static @NonNull GitLabCommitLink parseURL(@NonNull URL url) {
        String[] pathArray = getPathArray(url);

        int length = pathArray.length;

        if (length < 4) {
            throw new IllegalArgumentException("GitLab commit SHA link must have at least 4 path segments");
        }

        if (length == 4 && pathArray[length - 3].equals(DASH_KEYWORD)) {
            throw new IllegalArgumentException("GitLab commit SHA link must have a project name, not just '-'");
        }

        int hashIndex = length - 1;
        int commitIndex = length - 2;
        int projectIndex = pathArray[length - 3].equals(DASH_KEYWORD) ? length - 4 : length - 3;

        checkPathItem(pathArray, hashIndex, HASH_PATTERN);
        checkPathItem(pathArray, commitIndex, COMMIT_KEYWORD);
        checkPathItem(pathArray, projectIndex, PROJECT_PATTERN);
        for (int i = projectIndex - 1; i >= 0; i--) {
            checkPathItem(pathArray, i, NAMESPACE_PATTERN);
        }

        return new GitLabCommitLink(baseOf(url), unmodifiableList(pathArray, 0, projectIndex), pathArray[projectIndex], pathArray[hashIndex]);
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @NonNull
    String hash;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(namespace).path(project).path(DASH_KEYWORD).path(COMMIT_KEYWORD).path(hash).toString();
    }

    private static final String DASH_KEYWORD = "-";
    private static final String COMMIT_KEYWORD = "commit";
}
