package internal.heylogs.github;

import internal.heylogs.GitHostLink;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubMentionLink implements GitHostLink {

    @StaticFactoryMethod
    public static @NonNull GitHubMentionLink parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubMentionLink(
                GitHostLink.urlOf(m.group("protocol") + "://" + m.group("host") + (m.group("port") != null ? (":" + parseInt(m.group("port"))) : "")),
                m.group("user"),
                m.group("organization"),
                m.group("teamName")
        );
    }

    @NonNull URL base;
    @Nullable String user;
    @Nullable String organization;
    @Nullable String teamName;

    @Override
    public String toString() {
        return isUser()
                ? (base + "/" + user)
                : (base + "/orgs/" + organization + "/teams/" + teamName);
    }

    public boolean isUser() {
        return user != null;
    }

    // https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#mentioning-people-and-teams
    private static final Pattern PATTERN = Pattern.compile("(?<protocol>https?)://(?<host>[^:/$]+)(?::(?<port>\\d+))?/(:?(?<user>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})|orgs/(?<organization>[^:/$]+)/teams/(?<teamName>[^:/$]+))");
}
