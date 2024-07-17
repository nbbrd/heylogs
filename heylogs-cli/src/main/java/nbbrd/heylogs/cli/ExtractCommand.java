package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.HeylogsOptions;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.heylogs.Filter;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.TimeRange;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "extract", description = "Extract versions from changelog.")
public final class ExtractCommand implements Callable<Void> {

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Option(
            names = {"-r", "--ref"},
            paramLabel = "<ref>",
            description = "Filter versions by name."
    )
    private String ref = Filter.DEFAULT.getRef();

    @CommandLine.Option(
            names = {"-u", "--unreleased"},
            paramLabel = "<pattern>",
            description = "Assume that versions that match this pattern are unreleased."
    )
    private Pattern unreleasedPattern = Filter.DEFAULT.getUnreleasedPattern();

    @CommandLine.Option(
            names = {"-f", "--from"},
            paramLabel = "<date>",
            description = "Filter versions by min date (included).",
            converter = LenientDateConverter.class
    )
    private LocalDate from = Filter.DEFAULT.getTimeRange().getFrom();

    @CommandLine.Option(
            names = {"-t", "--to"},
            paramLabel = "<date>",
            description = "Filter versions by max date (included).",
            converter = LenientDateConverter.class
    )
    private LocalDate to = Filter.DEFAULT.getTimeRange().getTo();

    @CommandLine.Option(
            names = {"-l", "--limit"},
            description = "Limit the number of versions."
    )
    private int limit = Filter.DEFAULT.getLimit();

    @CommandLine.Option(
            names = "--ignore-content",
            description = "Ignore versions content, keep headers only."
    )
    private boolean ignoreContent = false;

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
        store(extract(load()));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document extract(Document document) {
        heylogsOptions.initHeylogs().extract(document, getFilter());
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(output.getFile(), document);
    }

    public Filter getFilter() {
        return Filter
                .builder()
                .ref(ref)
                .unreleasedPattern(unreleasedPattern)
                .timeRange(TimeRange.of(from, to))
                .limit(limit)
                .ignoreContent(ignoreContent)
                .build();
    }

    private static final class LenientDateConverter implements CommandLine.ITypeConverter<LocalDate> {

        @Override
        public LocalDate convert(String value) {
            return Filter.parseLocalDate(value);
        }
    }
}
