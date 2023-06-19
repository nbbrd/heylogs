package nbbrd.heylogs.cli;

import nbbrd.heylogs.Checker;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.Rule;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static java.util.stream.Collectors.joining;

@Command(name = "debug", hidden = true)
public final class DebugCommand implements Callable<Void> {

    @Override
    public Void call() {
        Checker checker = Checker.ofServiceLoader();
        System.out.println("Rules: " + checker.getRules().stream().map(Rule::getId).collect(joining(", ")));
        System.out.println("Formats: " + checker.getFormats().stream().map(Format::getId).collect(joining(", ")));
        return null;
    }
}
