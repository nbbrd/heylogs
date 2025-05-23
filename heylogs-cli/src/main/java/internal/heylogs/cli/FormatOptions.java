package internal.heylogs.cli;

import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class FormatOptions {

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<id>",
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String formatId;
}
