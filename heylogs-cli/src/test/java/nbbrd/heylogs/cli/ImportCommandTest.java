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
public class ImportCommandTest {
    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ImportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);
        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }
    @Test
    public void testValidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ImportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);
        Path src = temp.resolve("changelog.json");
        Files.write(src, singletonList(
                "{\"title\":\"Changelog\",\"description\":null,"
                + "\"releases\":[{\"version\":null,\"link\":null,\"date\":null,"
                + "\"changes\":[{\"added\":\"First feature\"}],\"yanked\":false}]}"));
        Path out = temp.resolve("CHANGELOG.md");
        assertThat(cmd.execute(src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).contains("Imported");
        assertThat(out).exists().content(UTF_8)
                .contains("# Changelog").contains("## [Unreleased]").contains("First feature");
    }
    @Test
    public void testInvalidContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ImportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);
        Path src = temp.resolve("changelog.json");
        Files.write(src, singletonList("this is not valid json"));
        Path out = temp.resolve("CHANGELOG.md");
        assertThat(cmd.execute(src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(out).doesNotExist();
    }
    @Test
    public void testDryRun(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ImportCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);
        Path src = temp.resolve("changelog.json");
        Files.write(src, singletonList(
                "{\"title\":\"Changelog\",\"description\":null,"
                + "\"releases\":[{\"version\":null,\"link\":null,\"date\":null,"
                + "\"changes\":[],\"yanked\":false}]}"));
        Path out = temp.resolve("CHANGELOG.md");
        assertThat(cmd.execute("--dry-run", src.toString(), "-o", out.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getErr()).contains("Would import");
        assertThat(out).doesNotExist();
    }
    @Test
    public void testRoundTrip(@TempDir Path temp) throws IOException {
        Path markdown = temp.resolve("CHANGELOG.md");
        Files.write(markdown, Arrays.asList(
                "# Changelog", "",
                "## [Unreleased]", "", "### Added", "", "- New feature", "",
                "## [1.0.0] - 2024-01-01", "", "### Fixed", "", "- Fix bug", "",
                "[Unreleased]: https://github.com/example/project/compare/v1.0.0...HEAD",
                "[1.0.0]: https://github.com/example/project/releases/tag/v1.0.0"));
        Path json = temp.resolve("changelog.json");
        assertThat(new CommandLine(new ExportCommand()).execute(markdown.toString(), "-o", json.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(json).exists();
        Path result = temp.resolve("result.md");
        assertThat(new CommandLine(new ImportCommand()).execute(json.toString(), "-o", result.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(result).exists().content(UTF_8)
                .contains("# Changelog").contains("## [Unreleased]")
                .contains("## [1.0.0]").contains("New feature").contains("Fix bug");
    }
}