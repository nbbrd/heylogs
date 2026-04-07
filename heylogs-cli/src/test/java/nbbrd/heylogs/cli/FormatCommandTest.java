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

public class FormatCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new FormatCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testFormatSortedContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FormatCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "### Added",
                "",
                "- New feature",
                "",
                "### Fixed",
                "",
                "- Fix bug",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD"));

        assertThat(cmd.execute(src.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        String content = new String(Files.readAllBytes(src), UTF_8);
        assertThat(content.indexOf("### Added"))
                .isLessThan(content.indexOf("### Fixed"));
    }

    @Test
    public void testFormatUnsortedContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FormatCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "### Fixed",
                "",
                "- Fix bug",
                "",
                "### Added",
                "",
                "- New feature",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD"));

        assertThat(cmd.execute(src.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        String content = new String(Files.readAllBytes(src), UTF_8);
        assertThat(content.indexOf("### Added"))
                .isLessThan(content.indexOf("### Fixed"));
    }

    @Test
    public void testFormatMultipleFiles(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FormatCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src1 = temp.resolve("CHANGELOG1.md");
        Path src2 = temp.resolve("CHANGELOG2.md");
        for (Path src : Arrays.asList(src1, src2)) {
            Files.write(src, Arrays.asList(
                    "# Changelog",
                    "",
                    "## [Unreleased]",
                    "",
                    "### Fixed",
                    "",
                    "- Fix bug",
                    "",
                    "### Added",
                    "",
                    "- New feature",
                    "",
                    "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD"));
        }

        assertThat(cmd.execute(src1.toString(), src2.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        for (Path src : Arrays.asList(src1, src2)) {
            String content = new String(Files.readAllBytes(src), UTF_8);
            assertThat(content.indexOf("### Added"))
                    .isLessThan(content.indexOf("### Fixed"));
        }
    }

    @Test
    public void testCheckModeAlreadyFormatted(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FormatCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "### Added",
                "",
                "- New feature",
                "",
                "### Fixed",
                "",
                "- Fix bug",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD"));

        assertThat(cmd.execute("--check", src.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getErr()).isEmpty();
    }

    @Test
    public void testCheckModeNotFormatted(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FormatCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "### Fixed",
                "",
                "- Fix bug",
                "",
                "### Added",
                "",
                "- New feature",
                "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD"));

        String originalContent = new String(Files.readAllBytes(src), UTF_8);

        assertThat(cmd.execute("--check", src.toString()))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);

        // File must NOT have been modified in check mode
        assertThat(src).content(UTF_8).isEqualTo(originalContent);
    }
}
