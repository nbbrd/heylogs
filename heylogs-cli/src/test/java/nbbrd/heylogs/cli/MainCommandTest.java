package nbbrd.heylogs.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class MainCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new MainCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("scan", "check", "extract", "list");
        assertThat(watcher.getErr()).isEmpty();
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new MainCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--version")).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isNotEmpty()
                .contains("heylogs")
                .contains("JVM:")
                .contains("OS:");
        assertThat(watcher.getErr()).isEmpty();
    }
}
