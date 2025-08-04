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
class GitLabMergeRequestRef implements ForgeRef<GitLabMergeRequestLink> {

    @StaticFactoryMethod
    public static @NonNull GitLabMergeRequestRef parse(@NonNull CharSequence text) {
        return GitLabSupport.parseRef(
                (namespace, project, number) -> new GitLabMergeRequestRef(namespace, project, Integer.parseInt(number)),
                MERGE_REQUEST_SEPARATOR, NUMBER_PATTERN, true, text
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitLabMergeRequestRef of(@NonNull GitLabMergeRequestLink link, @NonNull GitLabRefType type) {
        switch (type) {
            case SAME_PROJECT:
                return new GitLabMergeRequestRef(null, null, link.getNumber());
            case SAME_NAMESPACE:
                return new GitLabMergeRequestRef(null, link.getProject(), link.getNumber());
            case CROSS_PROJECT:
                return new GitLabMergeRequestRef(link.getNamespace(), link.getProject(), link.getNumber());
            default:
                throw new RuntimeException();
        }
    }

    @Nullable
    List<String> namespace;

    @Nullable
    String project;

    int number;

    @Override
    public String toString() {
        return refToString(namespace, project, MERGE_REQUEST_SEPARATOR, String.valueOf(number));
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitLabMergeRequestLink link) {
        switch (getType()) {
            case SAME_PROJECT:
                return link.getNumber() == number;
            case SAME_NAMESPACE:
                return link.getProject().equals(project) && link.getNumber() == number;
            case CROSS_PROJECT:
                return link.getNamespace().equals(namespace) && link.getProject().equals(project) && link.getNumber() == number;
            default:
                throw new RuntimeException();
        }
    }

    public @NonNull GitLabRefType getType() {
        return getRefType(namespace, project);
    }

    private static final char MERGE_REQUEST_SEPARATOR = '!';
}
