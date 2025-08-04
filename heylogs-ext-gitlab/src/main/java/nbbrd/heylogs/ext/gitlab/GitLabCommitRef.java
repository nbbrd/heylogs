package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static nbbrd.heylogs.ext.gitlab.GitLabSupport.*;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCommitRef implements ForgeRef<GitLabCommitLink> {

    @StaticFactoryMethod
    public static @NonNull GitLabCommitRef parse(@NonNull CharSequence text) {
        return GitLabSupport.parseRef(GitLabCommitRef::new, HASH_SEPARATOR, HASH_PATTERN, false, text);
    }

    @StaticFactoryMethod
    public static @NonNull GitLabCommitRef of(@NonNull GitLabCommitLink link, @NonNull GitLabRefType type) {
        switch (type) {
            case SAME_PROJECT:
                return new GitLabCommitRef(null, null, link.getHash().substring(0, 7));
            case SAME_NAMESPACE:
                return new GitLabCommitRef(null, link.getProject(), link.getHash().substring(0, 7));
            case CROSS_PROJECT:
                return new GitLabCommitRef(link.getNamespace(), link.getProject(), link.getHash().substring(0, 7));
            default:
                throw new RuntimeException();
        }
    }

    @Nullable
    List<String> namespace;

    @Nullable
    String project;

    @NonNull
    String hash;

    @Override
    public String toString() {
        return refToString(namespace, project, HASH_SEPARATOR, hash);
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitLabCommitLink link) {
        switch (getType()) {
            case SAME_PROJECT:
                return link.getHash().startsWith(hash);
            case SAME_NAMESPACE:
                return link.getProject().equals(project) && link.getHash().startsWith(hash);
            case CROSS_PROJECT:
                return link.getNamespace().equals(namespace) && link.getProject().equals(project) && link.getHash().startsWith(hash);
            default:
                throw new RuntimeException();
        }
    }

    public @NonNull GitLabRefType getType() {
        return getRefType(namespace, project);
    }

    private static final char HASH_SEPARATOR = '@';
}
