package internal.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import lombok.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Locale.ROOT;

public final class FeedbackSupport {

    private static final List<Ansi.IStyle> SUCCESS_STYLES =
            Arrays.asList(Ansi.Style.bold, Ansi.Style.fg_green);

    private static final List<Ansi.IStyle> WARNING_STYLES =
            Arrays.asList(Ansi.Style.bold, Ansi.Style.fg_yellow);

    private static final List<Ansi.IStyle> DRY_RUN_STYLES =
            Arrays.asList(Ansi.Style.bold, Ansi.Style.fg_cyan);

    private static final List<Ansi.IStyle> NOOP_STYLES =
            Collections.singletonList(Ansi.Style.faint);

    private FeedbackSupport() {
    }

    public static void printSuccess(@NonNull CommandLine.Model.CommandSpec spec, @NonNull String message) {
        printLine(spec, indicator(spec, "+", SUCCESS_STYLES) + " " + message);
    }

    public static void printWarning(@NonNull CommandLine.Model.CommandSpec spec, @NonNull String message) {
        printLine(spec, indicator(spec, "!", WARNING_STYLES) + " " + message);
    }

    public static void printDryRun(@NonNull CommandLine.Model.CommandSpec spec, @NonNull String message) {
        printLine(spec, indicator(spec, "~", DRY_RUN_STYLES) + " " + message);
    }

    public static void printNoOp(@NonNull CommandLine.Model.CommandSpec spec, @NonNull String message) {
        printLine(spec, spec.commandLine().getColorScheme().apply("= " + message, NOOP_STYLES).toString());
    }

    public static @NonNull String faint(@NonNull CommandLine.Model.CommandSpec spec, @NonNull String text) {
        return spec.commandLine().getColorScheme().apply(text, NOOP_STYLES).toString();
    }

    public static @NonNull String relativize(@NonNull Path file) {
        try {
            return Paths.get("").toAbsolutePath().relativize(file.toAbsolutePath()).toString();
        } catch (IllegalArgumentException e) {
            return file.toAbsolutePath().toString();
        }
    }

    public static boolean isBatchMode(@NonNull CommandLine.Model.CommandSpec spec) {
        CommandLine.ParseResult parseResult = spec.root().commandLine().getParseResult();
        return parseResult != null && parseResult.hasMatchedOption(SpecialProperties.BATCH_OPTION);
    }

    public static @NonNull String formatElapsed(long startNano) {
        long ms = (System.nanoTime() - startNano) / 1_000_000L;
        return ms < 1000 ? ms + "ms" : String.format(ROOT, "%.1fs", ms / 1000.0);
    }

    public static @NonNull String renderDocument(@NonNull Document document) {
        return FlexmarkIO.newFormatter().render(document);
    }

    private static @NonNull String indicator(
            @NonNull CommandLine.Model.CommandSpec spec,
            @NonNull String symbol,
            @NonNull List<Ansi.IStyle> styles) {
        return spec.commandLine().getColorScheme().apply(symbol, styles).toString();
    }

    private static void printLine(@NonNull CommandLine.Model.CommandSpec spec, @NonNull String line) {
        if (!isBatchMode(spec)) {
            spec.commandLine().getErr().println(line);
        }
    }
}
