package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.BlobLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.URLExtractor.*;

// https://docs.gitlab.com/user/project/repository/files/
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabBlobLink implements GitLabProjectLink, BlobLink {

    @StaticFactoryMethod
    public static @NonNull GitLabBlobLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);
        int length = pathArray.length;
        // Minimum: [namespace, project, "-", "blob", ref, file]
        if (length < 6) {
            throw new IllegalArgumentException("GitLab blob link must have at least 6 path segments");
        }
        // Find the "-" separator
        int dashIndex = -1;
        for (int i = 1; i < length - 3; i++) {
            if (GitLabSupport.DASH_KEYWORD.equals(pathArray[i])) {
                dashIndex = i;
                break;
            }
        }
        if (dashIndex == -1) {
            throw new IllegalArgumentException("GitLab blob link must contain '-' separator");
        }
        int blobIndex = dashIndex + 1;
        int refIndex = dashIndex + 2;
        int filePathStart = dashIndex + 3;
        int projectIndex = dashIndex - 1;
        checkPathItem(pathArray, blobIndex, BLOB_KEYWORD);
        checkPathItem(pathArray, refIndex, REF_PATTERN);
        checkPathItem(pathArray, projectIndex, GitLabSupport.PROJECT_PATTERN);
        for (int i = projectIndex - 1; i >= 0; i--) {
            checkPathItem(pathArray, i, GitLabSupport.NAMESPACE_PATTERN);
        }
        return new GitLabBlobLink(
                baseOf(url),
                Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(pathArray, 0, projectIndex))),
                pathArray[projectIndex],
                pathArray[refIndex],
                Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(pathArray, filePathStart, length)))
        );
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @NonNull
    String branchName;

    @NonNull
    List<String> filePath;

    @Override
    public String toString() {
        return toURL().toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(URLQueryBuilder.of(base)
                .path(namespace).path(project)
                .path(GitLabSupport.DASH_KEYWORD)
                .path(BLOB_KEYWORD).path(branchName)
                .path(filePath)
                .toString());
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    private static final String BLOB_KEYWORD = "blob";
    static final Pattern REF_PATTERN = Pattern.compile("[a-z\\d][a-z\\d._+\\-]*", Pattern.CASE_INSENSITIVE);
}