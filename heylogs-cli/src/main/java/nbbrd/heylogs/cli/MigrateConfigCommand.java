package nbbrd.heylogs.cli;

import nbbrd.heylogs.MigrationReport;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
        name = "migrate-config",
        description = "Migrate heylogs configuration from pom.xml to heylogs.properties.",
        hidden = true
)
public final class MigrateConfigCommand implements Callable<Integer> {

    @Option(
            names = {"-d", "--directory"},
            description = "Starting directory (default: current directory)."
    )
    private Path directory;

    @Override
    public Integer call() throws IOException {
        Path startDir = directory != null ? directory : Paths.get("").toAbsolutePath();

        MigrationReport report = MigrationReport.migrate(startDir);

        System.out.println(report);

        return report.hasErrors() ? 1 : 0;
    }
}


