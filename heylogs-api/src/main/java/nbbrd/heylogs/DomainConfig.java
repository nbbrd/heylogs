package nbbrd.heylogs;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeLoader;
import org.jspecify.annotations.NonNull;

import java.net.URI;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainConfig {

    @StaticFactoryMethod
    public static @NonNull DomainConfig parse(@NonNull CharSequence text) {
        String textString = text.toString();
        int index = textString.indexOf(':');
        if (index != -1) return of(textString.substring(0, index), textString.substring(index + 1));
        throw new IllegalArgumentException("Cannot find ':' in '" + textString + "'");
    }

    @StaticFactoryMethod
    public static @NonNull DomainConfig of(@NonNull String domain, @NonNull String forge) {
        if (!isValidDomain(domain)) {
            throw new IllegalArgumentException("Invalid domain '" + domain + "'");
        }
        if (!ForgeLoader.ID_PATTERN.matcher(forge).matches()) {
            throw new IllegalArgumentException("Invalid forge ID '" + forge + "'");
        }
        return new DomainConfig(domain, forge);
    }

    private static boolean isValidDomain(String domain) {
        try {
            return domain.equals(URI.create("http://" + domain).getHost());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @NonNull
    String domain;

    @NonNull
    String forgeId;

    @Override
    public String toString() {
        return domain + ":" + forgeId;
    }

    public boolean isCompatibleWith(@NonNull Forge other) {
        return this.forgeId.equals(other.getForgeId());
    }
}
