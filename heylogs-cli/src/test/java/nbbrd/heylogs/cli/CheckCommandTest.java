package nbbrd.heylogs.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new CheckCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testValidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new CheckCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("src.md");
        Files.write(src, singletonList("# Changelog"));

        Path out = temp.resolve("out.txt");

        assertThat(cmd.execute(src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(out)
                .content(UTF_8)
                .contains("No problem");
    }

    @Test
    public void testInvalidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new CheckCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("src.md");
        Files.write(src, singletonList("Changelog"));

        Path out = temp.resolve("out.txt");

        assertThat(cmd.execute(src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(out)
                .content(UTF_8)
                .contains("Missing Changelog heading");
    }
}
