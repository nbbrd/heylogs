package nbbrd.heylogs.ext.forgejo;

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
class ForgejoCommitSHARef implements ForgeRef<ForgejoCommitSHALink> {

    public enum Type {HASH, OWNER_HASH, OWNER_REPO_HASH}

    @StaticFactoryMethod
    public static @NonNull ForgejoCommitSHARef parse(@NonNull CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException(text.toString());
        return new ForgejoCommitSHARef(
                m.group("owner"),
                m.group("repo"),
                m.group("hash")
        );
    }

    @StaticFactoryMethod
    public static @NonNull ForgejoCommitSHARef of(@NonNull ForgejoCommitSHALink link, @NonNull Type type) {
        switch (type) {
            case HASH:
                return new ForgejoCommitSHARef(null, null, link.getHash().substring(0, 7));
            case OWNER_HASH:
                return new ForgejoCommitSHARef(link.getOwner(), null, link.getHash().substring(0, 7));
            case OWNER_REPO_HASH:
                return new ForgejoCommitSHARef(link.getOwner(), link.getRepo(), link.getHash().substring(0, 7));
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

    public boolean isCompatibleWith(@NonNull ForgejoCommitSHALink link) {
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

    // https://forgejo.org/docs/latest/user/linked-references/#commits
    // not found in docs but has same behavior/pattern as GitHub
    private static final Pattern PATTERN = Pattern.compile("((?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})(?:/(?<repo>[a-z\\d._-]{1,100}))?@)?(?<hash>[0-9a-f]{7})", Pattern.CASE_INSENSITIVE);
}
