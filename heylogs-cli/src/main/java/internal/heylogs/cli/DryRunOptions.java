package internal.heylogs.cli;

import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class DryRunOptions {

    @CommandLine.Option(
            names = {"--dry-run"},
            description = "Print what would be done without modifying any file."
    )
    private boolean dryRun;
}