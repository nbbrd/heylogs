package nbbrd.heylogs.cli;

import internal.heylogs.SemverRule;
import internal.heylogs.StylishFormat;
import internal.heylogs.cli.FormatCandidates;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.heylogs.Lister;
import nbbrd.heylogs.Resource;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "list", description = "List available resources.")
public final class ListCommand implements Callable<Void> {

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Option(
            names = {"-s", "--semver"},
            defaultValue = "false",
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<name>",
            defaultValue = StylishFormat.ID,
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String formatId;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Void call() throws IOException {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {
            Lister lister = getLister();
            lister.format(writer, Stream.concat(
                    lister.getRules().stream().map(rule -> new Resource("rule", rule.getRuleId())),
                    lister.getFormats().stream().map(format -> new Resource("format", format.getFormatId()))
            ).collect(toList()));
        }
        return null;
    }

    private Lister getLister() {
        Lister.Builder result = Lister.ofServiceLoader()
                .toBuilder()
                .formatId(formatId);
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }
}
