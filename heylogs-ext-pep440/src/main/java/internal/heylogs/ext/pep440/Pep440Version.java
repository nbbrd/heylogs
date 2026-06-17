package internal.heylogs.ext.pep440;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and compares versions conforming to
 * <a href="https://peps.python.org/pep-0440/">PEP 440</a>.
 * <p>
 * Canonical format: {@code N[.N]+[{a|b|rc}N][.postN][.devN]}
 * <p>
 * Ordering: {@code devN < aN < bN < rcN < (final) < .postN}
 */
public final class Pep440Version {

    private Pep440Version() {
        throw new UnsupportedOperationException();
    }

    // https://peps.python.org/pep-0440/#appendix-b-parsing-version-strings-with-regular-expressions
    private static final Pattern PATTERN = Pattern.compile(
            "(?<epoch>\\d+!)?"
                    + "(?<release>\\d+(?:\\.\\d+)*)"
                    + "(?:(?<pretype>a|b|rc)(?<prenum>\\d+))?"
                    + "(?:\\.post(?<postnum>\\d+))?"
                    + "(?:\\.dev(?<devnum>\\d+))?",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isValid(@NonNull CharSequence text) {
        return PATTERN.matcher(text).matches();
    }

    public static int compare(@NonNull CharSequence a, @NonNull CharSequence b) {
        Parsed pa = parse(a);
        Parsed pb = parse(b);
        if (pa == null || pb == null) return 0;
        return pa.compareTo(pb);
    }

    public static @Nullable String toFamily(@NonNull CharSequence version) {
        Parsed p = parse(version);
        if (p == null) return null;
        int[] rel = p.release;
        if (rel.length >= 2) {
            return rel[0] + "." + rel[1];
        }
        return String.valueOf(rel[0]);
    }

    private static @Nullable Parsed parse(CharSequence text) {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) return null;
        return new Parsed(
                parseOptionalInt(m.group("epoch")),
                parseRelease(m.group("release")),
                preTypeOrdinal(m.group("pretype")),
                parseOptionalInt(m.group("prenum")),
                parseOptionalInt(m.group("postnum")),
                parseOptionalInt(m.group("devnum"))
        );
    }

    private static int[] parseRelease(String release) {
        String[] parts = release.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }

    private static int parseOptionalInt(@Nullable String value) {
        if (value == null) return -1;
        // strip trailing '!' from epoch group
        if (value.endsWith("!")) {
            value = value.substring(0, value.length() - 1);
        }
        return Integer.parseInt(value);
    }

    /**
     * Maps pre-release type to an ordinal for comparison.
     * {@code a=0, b=1, rc=2, (none)=-1}
     */
    private static int preTypeOrdinal(@Nullable String type) {
        if (type == null) return -1;
        switch (type.toLowerCase(java.util.Locale.ROOT)) {
            case "a":
                return 0;
            case "b":
                return 1;
            case "rc":
                return 2;
            default:
                return -1;
        }
    }

    /**
     * Internal representation of a parsed PEP 440 version.
     * <p>
     * Ordering: epoch > release > pre > post > dev
     * <p>
     * A version with a pre-release tag sorts before the final release.
     * A version with a dev tag sorts before anything else at the same level.
     * A version with a post tag sorts after the final release.
     */
    private static final class Parsed implements Comparable<Parsed> {
        final int epoch;
        final int[] release;
        final int preType;   // -1=none, 0=a, 1=b, 2=rc
        final int preNum;    // -1=none
        final int postNum;   // -1=none
        final int devNum;    // -1=none

        Parsed(int epoch, int[] release, int preType, int preNum, int postNum, int devNum) {
            this.epoch = epoch < 0 ? 0 : epoch;
            this.release = release;
            this.preType = preType;
            this.preNum = preNum;
            this.postNum = postNum;
            this.devNum = devNum;
        }

        @Override
        public int compareTo(Parsed other) {
            // 1. epoch
            int cmp = Integer.compare(this.epoch, other.epoch);
            if (cmp != 0) return cmp;

            // 2. release segments (pad with zeros)
            int maxLen = Math.max(this.release.length, other.release.length);
            for (int i = 0; i < maxLen; i++) {
                int a = i < this.release.length ? this.release[i] : 0;
                int b = i < other.release.length ? other.release[i] : 0;
                cmp = Integer.compare(a, b);
                if (cmp != 0) return cmp;
            }

            // 3. pre-release phase
            // dev-only < pre-release < final < post
            cmp = Integer.compare(this.sortKey(), other.sortKey());
            if (cmp != 0) return cmp;

            // 4. within same phase, compare detail
            if (this.preType >= 0 && other.preType >= 0) {
                cmp = Integer.compare(this.preType, other.preType);
                if (cmp != 0) return cmp;
                cmp = Integer.compare(this.preNum, other.preNum);
                if (cmp != 0) return cmp;
            }

            cmp = Integer.compare(
                    this.postNum < 0 ? -1 : this.postNum,
                    other.postNum < 0 ? -1 : other.postNum
            );
            if (cmp != 0) return cmp;

            return Integer.compare(
                    this.devNum < 0 ? Integer.MAX_VALUE : this.devNum,
                    other.devNum < 0 ? Integer.MAX_VALUE : other.devNum
            );
        }

        /**
         * Returns a sort key for the phase of this version:
         * <ul>
         *   <li>0 = dev-only (no pre, no post, has dev)</li>
         *   <li>1 = pre-release (a/b/rc)</li>
         *   <li>2 = final or post</li>
         * </ul>
         */
        private int sortKey() {
            if (preType >= 0) return 1;
            if (postNum >= 0) return 2;
            if (devNum >= 0) return 0;
            return 2; // final
        }
    }
}
