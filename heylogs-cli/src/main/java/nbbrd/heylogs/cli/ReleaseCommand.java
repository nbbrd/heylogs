package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.ConfigOptions;
import internal.heylogs.cli.DryRunOptions;
import internal.heylogs.cli.FeedbackSupport;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Version;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "release", description = "Release changes.")
public final class ReleaseCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Option(
            names = {"-r", "--ref"},
            paramLabel = "<ref>",
            description = "Version reference.",
            required = true
    )
    private String ref;

    @CommandLine.Option(
            names = {"-d", "--date"},
            paramLabel = "<date>",
            description = "Release date (default : today).",
            required = false
    )
    private LocalDate date = LocalDate.now(ZoneId.systemDefault());

    @CommandLine.Mixin
    private ConfigOptions configOptions;

    @CommandLine.Mixin
    private DryRunOptions dryRunOptions;

    @Override
    public Void call() throws Exception {
        String displayPath = FeedbackSupport.relativize(input.getFile());
        if (dryRunOptions.isDryRun()) {
            FeedbackSupport.printDryRun(spec, "Would release [" + ref + "] - " + date + " in " + displayPath);
            return null;
        }
        store(release(load()));
        FeedbackSupport.printSuccess(spec, "Released [" + ref + "] - " + date + " in " + displayPath);
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document release(Document document) {
        Config config = configOptions.getConfigFromDirectory(Config.resolveStartDir(input.getFile()));
        Heylogs.ofServiceLoader().release(document, Version.of(ref, null, '-', date, false), config);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}
