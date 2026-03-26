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

public class PushCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new PushCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testPushToExistingGroup(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new PushCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "### Added",
                "",
                "- Existing entry",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-y", "added", "-m", "New feature"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(src)
                .content(UTF_8)
                .contains("### Added")
                .contains("- Existing entry")
                .contains("- New feature");
    }

    @Test
    public void testPushToNewGroup(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new PushCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-y", "fixed", "-m", "Fix critical bug"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(src)
                .content(UTF_8)
                .contains("### Fixed")
                .contains("- Fix critical bug");
    }

    @Test
    public void testPushWithMarkdownLinks(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new PushCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-y", "added", "-m", "Add check on GitHub Pull Request links [#173](https://github.com/nbbrd/heylogs/issues/173)"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(src)
                .content(UTF_8)
                .contains("### Added")
                .contains("[#173](https://github.com/nbbrd/heylogs/issues/173)");
    }

    @Test
    public void testMissingMessage(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new PushCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-y", "added"))
                .isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testInvalidType(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new PushCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path src = temp.resolve("CHANGELOG.md");
        Files.write(src, Arrays.asList(
                "# Changelog",
                "",
                "## [Unreleased]",
                "",
                "[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.9.3...HEAD"));

        assertThat(cmd.execute(src.toString(), "-y", "invalid", "-m", "Some change"))
                .isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }
}

