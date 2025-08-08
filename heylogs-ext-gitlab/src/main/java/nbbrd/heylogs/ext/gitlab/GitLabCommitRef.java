package nbbrd.heylogs.ext.gitlab;

import internal.heylogs.git.Hash;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static internal.heylogs.git.Hash.HASH_PATTERN;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.getRefType;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.refToString;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCommitRef implements ForgeRef<GitLabCommitLink> {

    @StaticFactoryMethod
    public static @NonNull GitLabCommitRef parse(@NonNull CharSequence text) {
        return GitLabSupport.parseRef(
                (namespace, project, hash) -> new GitLabCommitRef(namespace, project, Hash.parse(hash)),
                HASH_SEPARATOR, HASH_PATTERN, false, text
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitLabCommitRef of(@NonNull GitLabCommitLink link, @NonNull GitLabRefType type) {
        switch (type) {
            case SAME_PROJECT:
                return new GitLabCommitRef(null, null, link.getHash().toShort());
            case SAME_NAMESPACE:
                return new GitLabCommitRef(null, link.getProject(), link.getHash().toShort());
            case CROSS_PROJECT:
                return new GitLabCommitRef(link.getNamespace(), link.getProject(), link.getHash().toShort());
            default:
                throw new RuntimeException();
        }
    }

    @Nullable
    List<String> namespace;

    @Nullable
    String project;

    @NonNull
    Hash hash;

    @Override
    public String toString() {
        return refToString(namespace, project, HASH_SEPARATOR, hash.toString());
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitLabCommitLink link) {
        switch (getType()) {
            case SAME_PROJECT:
                return hash.isCompatibleWith(link.getHash());
            case SAME_NAMESPACE:
                return link.getProject().equals(project) && hash.isCompatibleWith(link.getHash());
            case CROSS_PROJECT:
                return link.getNamespace().equals(namespace) && link.getProject().equals(project) && hash.isCompatibleWith(link.getHash());
            default:
                throw new RuntimeException();
        }
    }

    public @NonNull GitLabRefType getType() {
        return getRefType(namespace, project);
    }

    private static final char HASH_SEPARATOR = '@';
}
