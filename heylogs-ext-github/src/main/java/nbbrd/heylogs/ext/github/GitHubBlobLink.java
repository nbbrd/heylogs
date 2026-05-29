package nbbrd.heylogs.ext.github;

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

// https://docs.github.com/en/repositories/working-with-files/using-files/getting-permanent-links-to-files
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubBlobLink implements GitHubProjectLink, BlobLink {

    @StaticFactoryMethod
    public static @NonNull GitHubBlobLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);
        if (pathArray.length < 5) {
            throw new IllegalArgumentException("GitHub blob link must have at least 5 path segments, found " + pathArray.length);
        }
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, BLOB_KEYWORD);
        checkPathItem(pathArray, 3, REF_PATTERN);
        return new GitHubBlobLink(
                baseOf(url),
                pathArray[0],
                pathArray[1],
                pathArray[3],
                Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(pathArray, 4, pathArray.length)))
        );
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    String branchName;

    @NonNull
    List<String> filePath;

    @Override
    public String toString() {
        return URLQueryBuilder.of(base).path(owner).path(repo).path(BLOB_KEYWORD).path(branchName).path(filePath).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    private static final String BLOB_KEYWORD = "blob";
    static final Pattern REF_PATTERN = Pattern.compile("[a-z\\d][a-z\\d._+\\-]*", Pattern.CASE_INSENSITIVE);
}