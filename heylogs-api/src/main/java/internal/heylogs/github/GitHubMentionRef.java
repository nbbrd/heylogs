package internal.heylogs.github;

import internal.heylogs.GitHostRef;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RepresentableAsString
@lombok.Value(staticConstructor = "of")
class GitHubMentionRef implements GitHostRef<GitHubMentionLink> {

    @StaticFactoryMethod
    public static @NonNull GitHubMentionRef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubMentionRef(
                m.group("user"),
                m.group("organization"),
                m.group("teamName")
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitHubMentionRef of(@NonNull GitHubMentionLink link) {
        return new GitHubMentionRef(link.getUser(), link.getOrganization(), link.getTeamName());
    }

    @Nullable String user;
    @Nullable String organization;
    @Nullable String teamName;

    @Override
    public String toString() {
        return isUser()
                ? "@" + user
                : "@" + organization + "/" + teamName;
    }

    public boolean isUser() {
        return user != null;
    }

    public boolean isCompatibleWith(@NonNull GitHubMentionLink link) {
        return isUser()
                ? link.isUser() && Objects.equals(link.getUser(), getUser())
                : !link.isUser() && Objects.equals(link.getOrganization(), getOrganization()) && Objects.equals(link.getTeamName(), getTeamName());
    }

    // https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#mentioning-people-and-teams
    private static final Pattern PATTERN = Pattern.compile("@(?<user>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})|@(?<organization>[^:/$]+)/(?<teamName>[^:/$]+)");
}
