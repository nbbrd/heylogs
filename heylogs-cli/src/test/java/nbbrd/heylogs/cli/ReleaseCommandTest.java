package nbbrd.heylogs.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReleaseCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ReleaseCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testValidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ReleaseCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("src.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "## [Unreleased]",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "--ref", "1.0.0"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(src)
                .content(UTF_8)
                .contains(
                        "## [Unreleased]",
                        "## [1.0.0] - ",
                        "[Unreleased]: https://github.com/nbbrd/heylogs/compare/1.0.0...HEAD",
                        "[1.0.0]: https://github.com/nbbrd/heylogs/compare/v0.9.3...1.0.0"
                );
    }

    @Test
    public void testInvalidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ReleaseCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("src.md");
        Files.write(src, singletonList("Changelog"));

        assertThat(cmd.execute(src.toString(), "--ref", "1.0.0"))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();
    }

    @Test
    public void testInvalidOptions(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ReleaseCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("src.md");
        Files.write(src, singletonList("Changelog"));

        assertThat(cmd.execute(src.toString()))
                .isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }
}
