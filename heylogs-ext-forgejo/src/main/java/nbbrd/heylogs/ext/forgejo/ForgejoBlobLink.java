package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.BlobLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.UriQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.URLExtractor.*;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoBlobLink implements ForgejoProjectLink, BlobLink {

    @StaticFactoryMethod
    public static @NonNull ForgejoBlobLink parse(@NonNull URL url) {
        String[] pathArray = getPathArray(url);
        // Minimum: [owner, repo, "src", type, ref, file]
        if (pathArray.length < 6) {
            throw new IllegalArgumentException("Forgejo blob link must have at least 6 path segments, found " + pathArray.length);
        }
        checkPathItem(pathArray, 0, OWNER_PATTERN);
        checkPathItem(pathArray, 1, REPO_PATTERN);
        checkPathItem(pathArray, 2, SRC_KEYWORD);
        checkPathItem(pathArray, 3, REF_TYPE_BRANCH, REF_TYPE_TAG, REF_TYPE_COMMIT);
        checkPathItem(pathArray, 4, REF_PATTERN);
        return new ForgejoBlobLink(
                baseOf(url),
                pathArray[0],
                pathArray[1],
                pathArray[3],
                pathArray[4],
                Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(pathArray, 5, pathArray.length)))
        );
    }

    @NonNull
    URL base;

    @NonNull
    String owner;

    @NonNull
    String repo;

    @NonNull
    String refType;

    @NonNull
    String branchName;

    @NonNull
    List<String> filePath;

    @Override
    public String toString() {
        return UriQueryBuilder.of(uriOf(base)).path(owner).path(repo).path(SRC_KEYWORD).path(refType).path(branchName).path(filePath).toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    private static final String SRC_KEYWORD = "src";
    static final String REF_TYPE_BRANCH = "branch";
    static final String REF_TYPE_TAG = "tag";
    static final String REF_TYPE_COMMIT = "commit";
    static final Pattern REF_PATTERN = Pattern.compile("[a-z\\d][a-z\\d._+\\-]*", Pattern.CASE_INSENSITIVE);
}