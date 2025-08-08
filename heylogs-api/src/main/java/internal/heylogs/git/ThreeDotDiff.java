package internal.heylogs.git;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-comparing-branches-in-pull-requests#three-dot-and-two-dot-git-diff-comparisons
// https://stackoverflow.com/questions/9834689/how-can-i-see-the-differences-between-two-branches#9834872
@RepresentableAsString
@lombok.Value
public class ThreeDotDiff {

    public static @NonNull ThreeDotDiff parse(@NonNull CharSequence text) {
        Matcher matcher = THREE_DOT_DIFF_PATTERN.matcher(text);
        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid format: " + text);
        return new ThreeDotDiff(matcher.group(1), matcher.group(2));
    }

    @NonNull
    String from;

    @NonNull
    String to;

    @Override
    public String toString() {
        return from + "..." + to;
    }

    public @NonNull ThreeDotDiff derive(@NonNull String tag) {
        return to.equals("HEAD")
                ? from.equals("HEAD") ? new ThreeDotDiff(tag, tag) : new ThreeDotDiff(from, tag)
                : new ThreeDotDiff(to, tag);
    }

    public static final Pattern THREE_DOT_DIFF_PATTERN = Pattern.compile("(.+)\\.{3}(.+)", Pattern.CASE_INSENSITIVE);
}
