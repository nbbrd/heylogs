package internal.heylogs;

public final class HeylogsParameters {

    private HeylogsParameters() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String DEFAULT_CHANGELOG_FILE = "CHANGELOG.md";
    public static final String DEFAULT_RECURSIVE = "false";
    public static final String DEFAULT_SEMVER = "false";
    public static final String DEFAULT_FORMAT_ID = StylishFormat.ID;
}
