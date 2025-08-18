package internal.heylogs.ext.calver;

import java.util.Arrays;
import java.util.Optional;

@lombok.AllArgsConstructor
@lombok.Getter
public enum CalVerSeparator implements CalVerToken {

    SEP_DOT('.'),
    SEP_UNDERSCORE('_'),
    SEP_DASH('-');

    private final char separator;

    @Override
    public String toString() {
        return String.valueOf(separator);
    }

    static Optional<CalVerSeparator> lookupSeparator(char c) {
        return Arrays.stream(values())
                .filter(token -> c == token.separator)
                .findFirst();
    }
}
