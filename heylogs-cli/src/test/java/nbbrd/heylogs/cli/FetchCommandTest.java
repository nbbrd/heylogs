package nbbrd.heylogs.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new FetchCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testMissingUrl(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-t", "added"))
                .isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testMissingType(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-i", "https://github.com/nbbrd/heylogs/issues/1"))
                .isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testUnknownForge(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-y", "added", "-i", "https://unknown.example.com/issues/1"))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();
    }
}
