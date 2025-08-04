package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static internal.heylogs.spi.URLExtractor.checkPathItem;
import static java.util.Collections.unmodifiableList;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.NAMESPACE_PATTERN;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.PATH_SEPARATOR;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabMentionRef implements ForgeRef<GitLabMentionLink> {

    @StaticFactoryMethod
    public static @NonNull GitLabMentionRef parse(@NonNull CharSequence text) {
        if (text.length() < 2 || text.charAt(0) != '@') {
            throw new IllegalArgumentException("GitLab mention must start with '@'");
        }

        String[] pathArray = text.subSequence(1, text.length()).toString().split(PATH_SEPARATOR, -1);

        int length = pathArray.length;

        if (length < 1) {
            throw new IllegalArgumentException("GitLab mention ref must have at least 1 path segment");
        }

        for (int i = length - 1; i >= 0; i--) {
            checkPathItem(pathArray, i, NAMESPACE_PATTERN);
        }

        return new GitLabMentionRef(unmodifiableList(Arrays.asList(pathArray)));
    }

    @StaticFactoryMethod
    public static @NonNull GitLabMentionRef of(@NonNull GitLabMentionLink link) {
        return new GitLabMentionRef(link.getNamespace());
    }

    @NonNull
    List<String> namespace;

    @Override
    public String toString() {
        return "@" + String.join(PATH_SEPARATOR, namespace);
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitLabMentionLink link) {
        return Objects.equals(link.getNamespace(), getNamespace());
    }
}
