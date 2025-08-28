package nbbrd.heylogs.ext.github;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubRequestRef implements ForgeRef<GitHubRequestLink> {

    public enum Type {NUMBER, OWNER_REPO_NUMBER}

    @StaticFactoryMethod
    public static @NonNull GitHubRequestRef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubRequestRef(
                m.group("owner"),
                m.group("repo"),
                parseInt(m.group("issueNumber"))
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitHubRequestRef of(@NonNull GitHubRequestLink link, @Nullable GitHubRequestRef baseRef) {
        return of(link, baseRef == null ? GitHubRequestRef.Type.NUMBER : baseRef.getType());
    }

    @StaticFactoryMethod
    public static @NonNull GitHubRequestRef of(@NonNull GitHubRequestLink link, @NonNull Type type) {
        return type.equals(Type.NUMBER)
                ? new GitHubRequestRef(null, null, link.getRequestNumber())
                : new GitHubRequestRef(link.getOwner(), link.getRepo(), link.getRequestNumber());
    }

    @Nullable
    String owner;

    @Nullable
    String repo;

    int requestNumber;

    @Override
    public String toString() {
        return getType().equals(Type.NUMBER) ? "#" + requestNumber : owner + "/" + repo + "#" + requestNumber;
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitHubRequestLink link) {
        return (getType().equals(Type.NUMBER) || (link.getOwner().equals(owner) && link.getRepo().equals(repo)))
                && link.getRequestNumber() == requestNumber;
    }

    public @NonNull Type getType() {
        return !(owner != null && repo != null) ? Type.NUMBER : Type.OWNER_REPO_NUMBER;
    }

    // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
    private static final Pattern PATTERN = Pattern.compile("((?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})/(?<repo>[a-z\\d._-]{1,100}))?#(?<issueNumber>\\d+)", Pattern.CASE_INSENSITIVE);
}
