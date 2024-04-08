package internal.heylogs.cli;

import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;


@lombok.Getter
@lombok.Setter
public class ChangelogInputParameters {

    @CommandLine.Parameters(
            paramLabel = "<source>",
            description = "Input file (default: CHANGELOG.md).",
            defaultValue = "CHANGELOG.md"
    )
    private Path file = Paths.get("CHANGELOG.md");
}
