package nbbrd.heylogs.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new InitCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("--help")).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testInitCreatesFile(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new InitCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path file = temp.resolve("CHANGELOG.md");

        assertThat(cmd.execute(file.toString())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(file)
                .exists()
                .content(UTF_8)
                .contains("# Changelog")
                .contains("## [Unreleased]")
                .contains("Keep a Changelog");
    }

    @Test
    public void testInitFailsIfFileAlreadyExists(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new InitCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path file = temp.resolve("CHANGELOG.md");
        Files.write(file, "# Existing".getBytes(UTF_8));

        assertThat(cmd.execute(file.toString())).isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();

        // File content should be unchanged
        assertThat(file).content(UTF_8).isEqualTo("# Existing");
    }

    @Test
    public void testInitWithCustomTemplate(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new InitCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path templateFile = temp.resolve("custom.mustache");
        Files.write(templateFile, "# My Custom Changelog\n\n## [Unreleased]\n".getBytes(UTF_8));

        Path file = temp.resolve("CHANGELOG.md");

        assertThat(cmd.execute(file.toString(), "--template", templateFile.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(file)
                .exists()
                .content(UTF_8)
                .contains("# My Custom Changelog")
                .contains("## [Unreleased]");
    }
}

