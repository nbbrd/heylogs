package internal.heylogs.cli;

import internal.heylogs.StylishFormat;
import picocli.CommandLine;

@lombok.Getter
public class FormatOptions {

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<id>",
            defaultValue = StylishFormat.ID,
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String formatId;
}
