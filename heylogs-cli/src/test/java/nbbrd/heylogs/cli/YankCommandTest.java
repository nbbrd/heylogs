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
import static org.assertj.core.api.Assertions.assertThat;

public class YankCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new YankCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testYankRelease(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new YankCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "## [1.0.0] - 2020-01-01",
                "",
                "### Added",
                "",
                "- Initial release",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD",
                "[1.0.0]: https://github.com/example/project/releases/tag/v1.0.0"));

        assertThat(cmd.execute(src.toString(), "-r", "1.0.0"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(src)
                .content(UTF_8)
                .contains("## [1.0.0] - 2020-01-01 [YANKED]");
    }

    @Test
    public void testYankUnknownVersion(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new YankCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD"));

        assertThat(cmd.execute(src.toString(), "-r", "9.9.9"))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
    }

    @Test
    public void testYankAlreadyYanked(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new YankCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "## [1.0.0] - 2020-01-01 [YANKED]",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD",
                "[1.0.0]: https://github.com/example/project/releases/tag/v1.0.0",
                "[YANKED]: https://github.com/example/project/releases/tag/v1.0.0"));

        assertThat(cmd.execute(src.toString(), "-r", "1.0.0"))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
    }
}

