package internal.heylogs.maven.plugin;

import java.io.File;

public final class HeylogsParameters {

    private HeylogsParameters() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String INPUT_FILE_PROPERTY = "heylogs.input.file";

    public static final String WORKING_DIR_CHANGELOG = "CHANGELOG.md";

    public static final String FORMAT_ID_PROPERTY = "heylogs.format.id";

    public static final String OUTPUT_FILE_PROPERTY = "heylogs.output.file";

    public static final String MOJO_LOG_FILE = "";

    public static boolean isMojoLogFile(File outputFile) {
        return outputFile == null || outputFile.toString().isEmpty();
    }
}
