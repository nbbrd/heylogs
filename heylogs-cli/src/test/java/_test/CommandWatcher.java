package _test;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CommandWatcher {

    public static CommandWatcher on(CommandLine cmd) {
        CommandWatcher result = new CommandWatcher();
        result.register(cmd);
        return result;
    }

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();
    private Exception executionException = null;

    public String getOut() {
        return out.toString();
    }

    public String getErr() {
        return err.toString();
    }

    public Exception getExecutionException() {
        return executionException;
    }

    public void reset() {
        out.getBuffer().setLength(0);
        err.getBuffer().setLength(0);
        executionException = null;
    }

    private void register(CommandLine cmd) {
        cmd.setOut(new PrintWriter(out));
        cmd.setErr(new PrintWriter(err));
        cmd.setExecutionExceptionHandler(this::handleExecutionException);
    }

    private int handleExecutionException(Exception ex, CommandLine cmdx, CommandLine.ParseResult parseResult) {
        executionException = ex;
        return cmdx.getExitCodeExceptionMapper() != null
                ? cmdx.getExitCodeExceptionMapper().getExitCode(ex)
                : cmdx.getCommandSpec().exitCodeOnExecutionException();
    }
}
