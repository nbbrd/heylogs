package nbbrd.heylogs.cli;

import internal.heylogs.cli.FormatOptions;
import internal.heylogs.cli.HeylogsOptions;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "list", description = "List available resources.")
public final class ListCommand implements Callable<Void> {

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Mixin
    private HeylogsOptions heylogsOptions;

    @CommandLine.Mixin
    private FormatOptions formatOptions;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Void call() throws IOException {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {
            Heylogs heylogs = heylogsOptions.initHeylogs();
            heylogs.formatResources(formatOptions.getFormatId(), writer, heylogs.getResources());
        }
        return null;
    }
}
