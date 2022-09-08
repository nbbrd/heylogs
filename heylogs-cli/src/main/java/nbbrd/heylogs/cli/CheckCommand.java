package nbbrd.heylogs.cli;

import internal.heylogs.cli.MarkdownInputOptions;
import nbbrd.heylogs.ExtendedRules;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.GuidingPrinciples;
import nbbrd.heylogs.Rule;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "check")
public final class CheckCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @Override
    public Integer call() throws Exception {
        List<Rule> rules = getRules();

        List<Failure> failures = Failure.allOf(input.read(), rules);

        printStylish(input.getFile(), failures);

        return failures.isEmpty() ? CommandLine.ExitCode.OK : CommandLine.ExitCode.USAGE;
    }

    @NotNull
    private static List<Rule> getRules() {
        return Stream.concat(Stream.of(GuidingPrinciples.values()), Stream.of(ExtendedRules.values()))
                .map(Rule.class::cast)
                .collect(Collectors.toList());
    }

    // https://eslint.org/docs/latest/user-guide/formatters/#stylish
    private static void printStylish(Path inputFile, List<Failure> failures) {
        if (inputFile != null) {
            System.out.println(inputFile);
        }

        int l = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getLine())).max().orElse(0);
        int c = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getColumn())).max().orElse(0);
        int m = failures.stream().mapToInt(failure -> failure.getMessage().length()).max().orElse(0);

        failures.forEach(failure -> System.out.println(String.format("  %" + l + "d:%-" + c + "d  error  %-" + m + "s  %s", failure.getLine(), failure.getColumn(), failure.getMessage(), failure.getRule())));
    }

    private static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }
}
