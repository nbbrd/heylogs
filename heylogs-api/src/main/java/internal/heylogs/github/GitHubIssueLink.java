package internal.heylogs.github;

import internal.heylogs.GitHostLink;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubIssueLink implements GitHostLink {

    public static final String ISSUES_TYPE = "issues";
    public static final String PULL_REQUEST_TYPE = "pull";

    @StaticFactoryMethod
    public static @NonNull GitHubIssueLink parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubIssueLink(
                GitHostLink.urlOf(m.group("protocol") + "://" + m.group("host") + (m.group("port") != null ? (":" + parseInt(m.group("port"))) : "")),
                m.group("owner"),
                m.group("repo"),
                m.group("type"),
                parseInt(m.group("issueNumber"))
        );
    }

    @NonNull URL base;
    @NonNull String owner;
    @NonNull String repo;
    @NonNull String type;
    int issueNumber;

    @Override
    public String toString() {
        return base + "/" + owner + "/" + repo + "/" + type + "/" + issueNumber;
    }

    // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
    private static final Pattern PATTERN = Pattern.compile("(?<protocol>https?)://(?<host>[^:/$]+)(?::(?<port>\\d+))?/(?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})/(?<repo>[a-z\\d._-]{1,100})/(?<type>(issues|pull))/(?<issueNumber>\\d+)(?<issueComment>#issuecomment-(?<issueCommentNumber>\\d+))?");
}
