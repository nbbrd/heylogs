package nbbrd.heylogs.ext.gitlab;

import internal.heylogs.git.Hash;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;

import static internal.heylogs.git.Hash.HASH_PATTERN;
import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.linkToString;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.parseLink;

@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCommitLink implements ForgeLink {

    @StaticFactoryMethod
    public static @NonNull GitLabCommitLink parse(@NonNull URL url) {
        return parseLink(GitLabCommitLink::new, COMMIT_KEYWORD, HASH_PATTERN, Hash::parse, url);
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @NonNull
    Hash hash;

    @Override
    public String toString() {
        return linkToString(base, namespace, project, COMMIT_KEYWORD, hash.toString());
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(toString());
    }

    @Override
    public @NonNull ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return GitLabCommitRef.of(this, baseRef instanceof GitLabCommitRef ? (GitLabCommitRef) baseRef : null);
    }

    private static final String COMMIT_KEYWORD = "commit";
}
