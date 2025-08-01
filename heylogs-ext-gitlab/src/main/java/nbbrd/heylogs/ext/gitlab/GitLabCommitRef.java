package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.checkPathItem;
import static nbbrd.heylogs.ext.gitlab.GitLab.*;

// https://docs.gitlab.com/user/markdown/#gitlab-specific-references
// https://docs.gitlab.com/user/reserved_names/#limitations-on-usernames-project-and-group-names
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabCommitRef implements ForgeRef<GitLabCommitLink> {

    public enum Type {HASH, SAME_NAMESPACE, CROSS_PROJECT}

    @StaticFactoryMethod
    public static @NonNull GitLabCommitRef parse(@NonNull CharSequence text) {
        String textString = text.toString();

        int separatorIndex = textString.lastIndexOf(HASH_SEPARATOR);
        if (separatorIndex == -1) {
            if (!HASH_PATTERN.matcher(textString).matches())
                throw new IllegalArgumentException("Invalid commit reference: " + textString);
            return new GitLabCommitRef(null, null, textString);
        }

        String hash = textString.substring(separatorIndex + 1);
        if (!HASH_PATTERN.matcher(hash).matches())
            throw new IllegalArgumentException("Invalid commit hash in reference: " + textString);

        String[] parts = textString.substring(0, separatorIndex).split(Pattern.quote(PATH_SEPARATOR), -1);
        switch (parts.length) {
            case 0:
                return new GitLabCommitRef(null, null, hash);
            case 1:
                checkPathItem(parts, 0, PROJECT_PATTERN);
                return new GitLabCommitRef(null, parts[0], hash);
            default:
                int projectIndex = parts.length - 1;
                checkPathItem(parts, projectIndex, PROJECT_PATTERN);
                for (int i = projectIndex - 1; i >= 0; i--) {
                    checkPathItem(parts, i, NAMESPACE_PATTERN);
                }
                return new GitLabCommitRef(unmodifiableList(parts, 0, parts.length - 1), parts[parts.length - 1], hash);
        }
    }

    @StaticFactoryMethod
    public static @NonNull GitLabCommitRef of(@NonNull GitLabCommitLink link, @NonNull Type type) {
        switch (type) {
            case HASH:
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
        switch (getType()) {
            case HASH:
                return hash;
            case SAME_NAMESPACE:
                return project + HASH_SEPARATOR + hash;
            case CROSS_PROJECT:
                return String.join(PATH_SEPARATOR, namespace) + PATH_SEPARATOR + project + HASH_SEPARATOR + hash;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitLabCommitLink link) {
        switch (getType()) {
            case HASH:
                return link.getHash().startsWith(hash);
            case SAME_NAMESPACE:
                return project.equals(link.getProject()) && link.getHash().startsWith(hash);
            case CROSS_PROJECT:
                return namespace.equals(link.getNamespace()) && project.equals(link.getProject()) && link.getHash().startsWith(hash);
            default:
                throw new RuntimeException();
        }
    }

    public @NonNull Type getType() {
        return project != null ? (namespace != null ? Type.CROSS_PROJECT : Type.SAME_NAMESPACE) : Type.HASH;
    }

    private static final String PATH_SEPARATOR = "/";
    private static final char HASH_SEPARATOR = '@';
}
