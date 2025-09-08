package internal.heylogs.cli;

import nbbrd.heylogs.FormatConfig;
import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class FormatOptions {

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<id>",
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class,
            converter = FormatConverter.class
    )
    private FormatConfig format;
}
