package internal.heylogs.github;

import nbbrd.heylogs.spi.ForgeRef;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubIssueRef implements ForgeRef<GitHubIssueLink> {

    public enum Type {NUMBER, OWNER_REPO_NUMBER}

    @StaticFactoryMethod
    public static @NonNull GitHubIssueRef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubIssueRef(
                m.group("owner"),
                m.group("repo"),
                parseInt(m.group("issueNumber"))
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitHubIssueRef of(@NonNull GitHubIssueLink link, @NonNull Type type) {
        return type.equals(Type.NUMBER)
                ? new GitHubIssueRef(null, null, link.getIssueNumber())
                : new GitHubIssueRef(link.getOwner(), link.getRepo(), link.getIssueNumber());
    }

    @Nullable String owner;
    @Nullable String repo;
    int issueNumber;

    @Override
    public String toString() {
        return getType().equals(Type.NUMBER) ? "#" + issueNumber : owner + "/" + repo + "#" + issueNumber;
    }

    public boolean isCompatibleWith(@NonNull GitHubIssueLink link) {
        return (getType().equals(Type.NUMBER) || (link.getOwner().equals(owner) && link.getRepo().equals(repo)))
                && link.getIssueNumber() == issueNumber;
    }

    public @NonNull Type getType() {
        return !(owner != null && repo != null) ? Type.NUMBER : Type.OWNER_REPO_NUMBER;
    }

    // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
    private static final Pattern PATTERN = Pattern.compile("((?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})/(?<repo>[a-z\\d._-]{1,100}))?#(?<issueNumber>\\d+)");
}
