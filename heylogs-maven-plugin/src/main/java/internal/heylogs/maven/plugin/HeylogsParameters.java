package internal.heylogs.maven.plugin;

public final class HeylogsParameters {

    private HeylogsParameters() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String INPUT_FILES_PROPERTY = "heylogs.inputFiles";

    public static final String INPUT_FILE_PROPERTY = "heylogs.inputFile";

    public static final String WORKING_DIR_CHANGELOG = "CHANGELOG.md";

    public static final String FORMAT_ID_PROPERTY = "heylogs.formatId";

    public static final String OUTPUT_FILE_PROPERTY = "heylogs.outputFile";
}
