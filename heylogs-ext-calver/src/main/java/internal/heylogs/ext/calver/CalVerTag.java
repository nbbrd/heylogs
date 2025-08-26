package internal.heylogs.ext.calver;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@lombok.AllArgsConstructor
@lombok.Getter
public enum CalVerTag implements CalVerToken {

    TAG_YYYY(Pattern.compile("\\d{4}"), false),
    TAG_YY(Pattern.compile("\\d{1,2}"), false),
    TAG_0Y(Pattern.compile("\\d{2}"), false),
    TAG_MM(Pattern.compile("\\d{1,2}"), false),
    TAG_0M(Pattern.compile("\\d{2}"), false),
    TAG_WW(Pattern.compile("\\d{1,2}"), false),
    TAG_0W(Pattern.compile("\\d{2}"), false),
    TAG_DD(Pattern.compile("\\d{1,2}"), false),
    TAG_0D(Pattern.compile("\\d{2}"), false),
    TAG_MAJOR(Pattern.compile("\\d+"), false),
    TAG_MINOR(Pattern.compile("\\d+"), false),
    TAG_MICRO(Pattern.compile("\\d+"), true);

    private final Pattern pattern;
    private final boolean optional;

    @Override
    public String toString() {
        return name().substring(4);
    }

    public boolean isValidValue(@NonNull CharSequence value) {
        return pattern.matcher(value).matches();
    }

    static Optional<CalVerTag> lookupTag(CharSequence text) {
        return Arrays.stream(values())
                .filter(token -> text.equals(token.toString()))
                .findFirst();
    }
}
