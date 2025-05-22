package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.HeylogsOptions;
import internal.heylogs.cli.SpecialProperties;
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
            names = {"-t", "--tag-prefix"},
            paramLabel = "<prefix>",
            description = "Version tag prefix.",
            required = false
    )
    private String tagPrefix = "";

    @CommandLine.Option(
            names = {"-d", "--date"},
            paramLabel = "<date>",
            description = "Release date (default : today).",
            required = false
    )
    private LocalDate date = LocalDate.now(ZoneId.systemDefault());

    @CommandLine.Mixin
    private HeylogsOptions heylogsOptions;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Void call() throws Exception {
        store(release(load()));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document release(Document document) {
        heylogsOptions.initHeylogs().releaseChanges(document, Version.of(ref, '-', date), tagPrefix);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}
