package nbbrd.heylogs.ext.github;

import nbbrd.heylogs.spi.ForgeRef;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubMentionRef implements ForgeRef<GitHubMentionLink> {

    public enum Type {USER, TEAM}

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
        return getType().equals(Type.USER)
                ? "@" + user
                : "@" + organization + "/" + teamName;
    }

    public @NonNull Type getType() {
        return user != null ? Type.USER : Type.TEAM;
    }

    public boolean isCompatibleWith(@NonNull GitHubMentionLink link) {
        return getType().equals(Type.USER)
                ? link.isUser() && Objects.equals(link.getUser(), getUser())
                : !link.isUser() && Objects.equals(link.getOrganization(), getOrganization()) && Objects.equals(link.getTeamName(), getTeamName());
    }

    // https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#mentioning-people-and-teams
    private static final Pattern PATTERN = Pattern.compile("@(?<user>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})|@(?<organization>[^:/$]+)/(?<teamName>[^:/$]+)");
}
