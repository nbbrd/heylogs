package nbbrd.heylogs.ext.github;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitHubCommitSHARef implements ForgeRef<GitHubCommitSHALink> {

    public enum Type {HASH, OWNER_HASH, OWNER_REPO_HASH}

    @StaticFactoryMethod
    public static @NonNull GitHubCommitSHARef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new GitHubCommitSHARef(
                m.group("owner"),
                m.group("repo"),
                m.group("hash")
        );
    }

    @StaticFactoryMethod
    public static @NonNull GitHubCommitSHARef of(@NonNull GitHubCommitSHALink link, @NonNull Type type) {
        switch (type) {
            case HASH:
                return new GitHubCommitSHARef(null, null, link.getHash().substring(0, 7));
            case OWNER_HASH:
                return new GitHubCommitSHARef(link.getOwner(), null, link.getHash().substring(0, 7));
            case OWNER_REPO_HASH:
                return new GitHubCommitSHARef(link.getOwner(), link.getRepo(), link.getHash().substring(0, 7));
            default:
                throw new RuntimeException();
        }
    }

    @Nullable String owner;
    @Nullable String repo;
    @NonNull String hash;

    @Override
    public String toString() {
        switch (getType()) {
            case HASH:
                return hash;
            case OWNER_HASH:
                return owner + "@" + hash;
            case OWNER_REPO_HASH:
                return owner + "/" + repo + "@" + hash;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public boolean isCompatibleWith(@NonNull GitHubCommitSHALink link) {
        switch (getType()) {
            case HASH:
                return link.getHash().startsWith(hash);
            case OWNER_HASH:
                return owner.equals(link.getOwner()) && link.getHash().startsWith(hash);
            case OWNER_REPO_HASH:
                return owner.equals(link.getOwner()) && repo.equals(link.getRepo()) && link.getHash().startsWith(hash);
            default:
                throw new RuntimeException();
        }
    }

    public @NonNull Type getType() {
        return owner != null ? (repo != null ? Type.OWNER_REPO_HASH : Type.OWNER_HASH) : Type.HASH;
    }

    // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#commit-shas
    private static final Pattern PATTERN = Pattern.compile("((?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})(?:/(?<repo>[a-z\\d._-]{1,100}))?@)?(?<hash>[0-9a-f]{7})", Pattern.CASE_INSENSITIVE);
}
