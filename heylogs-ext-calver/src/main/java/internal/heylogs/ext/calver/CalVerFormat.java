package internal.heylogs.ext.calver;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static internal.heylogs.ext.calver.CalVerSeparator.lookupSeparator;
import static internal.heylogs.ext.calver.CalVerTag.*;

@RepresentableAsString
@lombok.Value
@lombok.Builder
public class CalVerFormat {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION = Pattern.compile("\\p{P}");
    private static final Pattern NON_NUMERIC = Pattern.compile("\\D+");

    public static @NonNull CalVerFormat parse(@NonNull CharSequence text) throws IllegalArgumentException {
        if (text.length() == 0) {
            throw new IllegalArgumentException("CalVer format cannot be empty");
        }

        if (WHITESPACE.matcher(text).find()) {
            throw new IllegalArgumentException("CalVer format cannot contain whitespace");
        }

        CalVerFormat.Builder result = CalVerFormat.builder();
        int index = 0;
        for (String tag : PUNCTUATION.split(text, -1)) {
            result.token(lookupTag(tag).orElseThrow(() -> new IllegalArgumentException("Unknown tag '" + tag + "'")));
            index += tag.length();
            if (index < text.length()) {
                char separator = text.charAt(index);
                result.token(lookupSeparator(separator).orElseThrow(() -> new IllegalArgumentException("Unknown separator '" + separator + "'")));
                index++;
            }
        }
        return result.build().validate();
    }

    @lombok.Singular
    List<CalVerToken> tokens;

    @Override
    public String toString() {
        return tokens.stream().map(Objects::toString).collect(joining());
    }

    public boolean isValidVersion(@NonNull CharSequence version) {
        if (version.length() == 0 || WHITESPACE.matcher(version).find()) {
            return false;
        }

        String[] tagValues = NON_NUMERIC.split(version, -1);

        int tagIndex = 0;
        int charIndex = 0;
        for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            CalVerToken token = tokens.get(tokenIndex);
            if (token instanceof CalVerTag) {
                CalVerTag tag = (CalVerTag) token;
                if (tagIndex < tagValues.length) {
                    String tagValue = tagValues[tagIndex];
                    if (!tag.isValidValue(tagValue)) {
                        return false;
                    }
                    charIndex += tagValue.length();
                }
                tagIndex++;
            } else {
                CalVerSeparator sep = (CalVerSeparator) token;
                if (!((CalVerTag) tokens.get(tokenIndex + 1)).isOptional()) {
                    if (charIndex >= version.length() || version.charAt(charIndex) != sep.getSeparator()) {
                        return false;
                    }
                }
                charIndex++;
            }
        }
        return true;
    }

    private CalVerFormat validate() {
        List<CalVerTag> onlyTags = tokens.stream().filter(CalVerTag.class::isInstance).map(CalVerTag.class::cast).collect(toList());
        checkDuplication(onlyTags);
        checkOrdering(onlyTags);
        checkCompatibility(onlyTags);
        return this;
    }

    private static void checkDuplication(List<CalVerTag> tags) {
        if (tags.stream().distinct().count() < tags.size()) {
            throw new IllegalArgumentException("CalVer format cannot have duplicated tags");
        }
    }

    private static void checkOrdering(List<CalVerTag> tags) {
        if (!Arrays.equals(tags.stream().sorted().toArray(CalVerTag[]::new), tags.toArray())) {
            throw new IllegalArgumentException("CalVer format must have ordered tags");
        }
    }

    private static void checkCompatibility(List<CalVerTag> tags) {
        if ((tags.contains(TAG_0W) || tags.contains(TAG_WW))
                && (tags.contains(TAG_0M) || tags.contains(TAG_MM) || tags.contains(TAG_0D) || tags.contains(TAG_DD))) {
            throw new IllegalArgumentException("CalVer format must not mix week and month/day tags");
        }
    }
}
