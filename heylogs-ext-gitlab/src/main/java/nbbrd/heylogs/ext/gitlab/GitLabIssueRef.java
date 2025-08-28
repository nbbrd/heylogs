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
class GitLabIssueRef implements ForgeRef<GitLabIssueLink> {

    @StaticFactoryMethod
    public static @NonNull GitLabIssueRef parse(@NonNull CharSequence text) {
        return GitLabSupport.parseRef(
                (namespace, project, number) -> new GitLabIssueRef(namespace, project, Integer.parseInt(number)),
                ISSUE_SEPARATOR, NUMBER_PATTERN, true, text
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitLabIssueRef of(@NonNull GitLabIssueLink link, @Nullable GitLabIssueRef baseRef) {
        return of(link, baseRef == null ? GitLabRefType.SAME_PROJECT : baseRef.getType());
    }

    @StaticFactoryMethod
    public static @NonNull GitLabIssueRef of(@NonNull GitLabIssueLink link, @NonNull GitLabRefType type) {
        switch (type) {
            case SAME_PROJECT:
                return new GitLabIssueRef(null, null, link.getNumber());
            case SAME_NAMESPACE:
                return new GitLabIssueRef(null, link.getProject(), link.getNumber());
            case CROSS_PROJECT:
                return new GitLabIssueRef(link.getNamespace(), link.getProject(), link.getNumber());
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
        return refToString(namespace, project, ISSUE_SEPARATOR, String.valueOf(number));
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitLabIssueLink link) {
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

    private static final char ISSUE_SEPARATOR = '#';
}
