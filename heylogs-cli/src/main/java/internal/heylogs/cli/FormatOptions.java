package internal.heylogs.cli;

import picocli.CommandLine;

import static internal.heylogs.HeylogsParameters.DEFAULT_FORMAT_ID;

@lombok.Getter
public class FormatOptions {

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<id>",
            defaultValue = DEFAULT_FORMAT_ID,
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String formatId;
}
