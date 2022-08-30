package internal.heylogs.cli;

import picocli.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;

@lombok.AllArgsConstructor
public final class PrintAndLogExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    @lombok.NonNull
    private final Class<?> logAnchor;

    private final boolean stackTraceRequired;

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
        reportToLogger(ex, parseResult);
        reportToConsole(ex, cmd);
        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : cmd.getCommandSpec().exitCodeOnExecutionException();
    }

    private void reportToLogger(Exception ex, CommandLine.ParseResult parseResult) {
        Logger logger = Logger.getLogger(logAnchor.getName());
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, "While executing command '" + String.join(" ", parseResult.originalArgs()) + "'", ex);
        }
    }

    private void reportToConsole(Exception ex, CommandLine cmd) {
        cmd.getErr().println(cmd.getColorScheme().errorText(getErrorMessage(ex)));
        if (stackTraceRequired) {
            cmd.getErr().println(cmd.getColorScheme().stackTraceText(ex));
        }
    }

    private String getErrorMessage(Exception ex) {
        return getLabel(ex) + ": " + ex.getMessage();
    }

    private String getLabel(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return "Invalid parameter";
        }
        return ex.getClass().getSimpleName();
    }
}
