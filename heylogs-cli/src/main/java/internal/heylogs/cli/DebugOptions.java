package internal.heylogs.cli;

import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class DebugOptions {

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;
}
