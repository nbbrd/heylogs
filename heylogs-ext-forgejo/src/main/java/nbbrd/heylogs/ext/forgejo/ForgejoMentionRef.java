package nbbrd.heylogs.ext.forgejo;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class ForgejoMentionRef implements ForgeRef<ForgejoMentionLink> {

    public enum Type {USER, TEAM}

    @StaticFactoryMethod
    public static @NonNull ForgejoMentionRef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new ForgejoMentionRef(
                m.group("user"),
                m.group("organization"),
                m.group("teamName")
        );
    }

    @StaticFactoryMethod
    public static @NonNull ForgejoMentionRef of(@NonNull ForgejoMentionLink link) {
        return new ForgejoMentionRef(link.getUser(), link.getOrganization(), link.getTeamName());
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

    @Override
    public boolean isCompatibleWith(@NonNull ForgejoMentionLink link) {
        return getType().equals(Type.USER)
                ? link.isUser() && Objects.equals(link.getUser(), getUser())
                : !link.isUser() && Objects.equals(link.getOrganization(), getOrganization()) && Objects.equals(link.getTeamName(), getTeamName());
    }

    // https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#mentioning-people-and-teams
    private static final Pattern PATTERN = Pattern.compile("@(?<user>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})|@(?<organization>[^:/$]+)/(?<teamName>[^:/$]+)", Pattern.CASE_INSENSITIVE);
}
