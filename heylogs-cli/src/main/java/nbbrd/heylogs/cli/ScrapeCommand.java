package nbbrd.heylogs.cli;

import internal.heylogs.base.StylishWriter;
import internal.heylogs.cli.ConfigOptions;
import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.MultiChangelogInputOptions;
import internal.heylogs.cli.SpecialProperties;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.ScrapedLink;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;

@Command(name = "scrape", description = "Scrape content.", hidden = true)
public final class ScrapeCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MultiChangelogInputOptions input;

    @CommandLine.Mixin
    private ConfigOptions configOptions;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Integer call() throws Exception {
        Heylogs heylogs = Heylogs.ofServiceLoader();
        Config config = configOptions.getConfig();

        MarkdownInputSupport inputSupport = newMarkdownInputSupport();

        List<ScrapedFile> list = new ArrayList<>();
        for (Path file : input.getAllFiles(inputSupport::accept)) {
            list.add(new ScrapedFile(file, heylogs.scrape(inputSupport.readDocument(file), config)));
        }

        StylishWriter
                .<ScrapedLink>builder()
                .column(Formatter.of(String::valueOf).compose(ScrapedLink::getLine))
                .column(Formatter.of(ScrapedLink::getLink))
                .column(Formatter.of(scrapedLink -> String.join(", ", scrapedLink.getTypes())))
                .build()
                .writeAll(System.out, list, ScrapedFile::getPathAsString, ScrapedFile::getLinks, ScrapedFile::summarize);

        return CommandLine.ExitCode.OK;
    }

    @lombok.Value
    private static class ScrapedFile {

        @NonNull
        Path path;

        @NonNull
        List<ScrapedLink> links;

        String getPathAsString() {
            return path.toString();
        }

        String summarize() {
            long emptyCount = links.stream().filter(link -> link.getTypes().isEmpty()).count();
            return (links.size() - emptyCount) + "/" + links.size() + " validated links";
        }
    }
}
