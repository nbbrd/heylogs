package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.spi.ForgeLink;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class GitLab {

    public static final String ID = "gitlab";

    static boolean isKnownHost(@NonNull ForgeLink expected) {
        return Arrays.asList(expected.getBase().getHost().split("\\.", -1)).contains("gitlab");
    }

    // https://docs.gitlab.com/user/reserved_names/#rules-for-usernames-project-and-group-names-and-slugs
    // FIXME: This regex is not perfect, it allows some invalid names.
    static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    static final Pattern PROJECT_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    static final Pattern HASH_PATTERN = Pattern.compile("[0-9a-f]{7,40}", Pattern.CASE_INSENSITIVE);


    @MightBePromoted
    static List<String> unmodifiableList(String[] array, int from, int to) {
        return Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(array, from, to)));
    }
}
