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

public class ExportCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ExportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testValidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ExportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "### Added",
                "",
                "- First feature",
                "",
                "## [1.0.0] - 2024-01-01",
                "",
                "### Fixed",
                "",
                "- Fix bug",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD",
                "[1.0.0]: https://github.com/example/project/releases/tag/v1.0.0"));

        Path out = temp.resolve("out.json");

        assertThat(cmd.execute(src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(out)
                .exists()
                .content(UTF_8)
                .contains("\"title\"")
                .contains("\"Changelog\"")
                .contains("\"versions\"")
                .contains("\"1.0.0\"")
                .contains("First feature");
    }

    @Test
    public void testInvalidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ExportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("src.md");
        Files.write(src, singletonList("Not a changelog"));

        Path out = temp.resolve("out.json");

        assertThat(cmd.execute(src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();

        assertThat(out).doesNotExist();
    }
}

