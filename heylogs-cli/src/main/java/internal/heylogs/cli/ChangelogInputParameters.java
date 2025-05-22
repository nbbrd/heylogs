package internal.heylogs.cli;

import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;


@lombok.Getter
@lombok.Setter
public class ChangelogInputParameters {

    @CommandLine.Parameters(
            paramLabel = "<source>",
            description = "Input file (default: CHANGELOG.md).",
            defaultValue = DEFAULT_CHANGELOG_FILE
    )
    private Path file = Paths.get(DEFAULT_CHANGELOG_FILE);
}
