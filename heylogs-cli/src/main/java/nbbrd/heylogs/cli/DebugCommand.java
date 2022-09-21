package nbbrd.heylogs.cli;

import nbbrd.heylogs.Rule;
import nbbrd.heylogs.RuleLoader;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "debug", hidden = true)
public final class DebugCommand implements Callable<Void> {

    @Override
    public Void call() {
        System.out.println("Rules: " + RuleLoader.load().stream().map(Rule::getName).collect(Collectors.joining(", ")));
        return null;
    }
}
