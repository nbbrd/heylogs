package internal.heylogs.github;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

@RepresentableAsString
@lombok.Value
class GitHubIssueRef {

    public static @NonNull GitHubIssueRef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubIssueRef(
                m.group("owner"),
                m.group("repo"),
                parseInt(m.group("issueNumber"))
        );
    }

    @Nullable String owner;
    @Nullable String repo;
    int issueNumber;

    @Override
    public String toString() {
        return hasOwnerRepo()
                ? owner + "/" + repo + "#" + issueNumber
                : "#" + issueNumber;
    }

    public boolean isCompatibleWith(@NonNull GitHubIssueLink link) {
        return (!hasOwnerRepo() || (link.getOwner().equals(owner) && link.getRepo().equals(repo)))
                && link.getIssueNumber() == issueNumber;
    }

    private boolean hasOwnerRepo() {
        return owner != null && repo != null;
    }

    // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
    private static final Pattern PATTERN = Pattern.compile("((?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})/(?<repo>[a-z\\d._-]{1,100}))?#(?<issueNumber>\\d+)");
}
