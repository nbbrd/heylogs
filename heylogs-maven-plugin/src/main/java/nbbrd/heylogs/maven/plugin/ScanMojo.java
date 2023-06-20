package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Scanner;
import nbbrd.heylogs.Status;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ScanMojo extends HeylogsMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.input.file")
    private File inputFile;

    @Parameter(defaultValue = StylishFormat.ID, property = "heylogs.format.id")
    private String formatId;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Scanning has been skipped.");
            return;
        }

        if (inputFile.exists()) {
            scan(loadScanner());
        } else {
            if (isRootProject(projectBaseDir)) {
                raiseErrorMissingChangelog();
            } else {
                notifyMissingChangelog();
            }
        }
    }

    private Scanner loadScanner() {
        return Scanner.ofServiceLoader()
                .toBuilder()
                .formatId(formatId)
                .build();
    }

    private void scan(Scanner scanner) throws MojoExecutionException {
        Document changelog = readChangelog(inputFile);
        Status status = scanner.scan(changelog);
        writeStatus(status, scanner);
    }

    private void writeStatus(Status status, Scanner scanner) throws MojoExecutionException {
        try {
            StringBuilder text = new StringBuilder();
            scanner.formatStatus(text, inputFile.toString(), status);
            new BufferedReader(new StringReader(text.toString())).lines().forEach(getLog()::info);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing status", ex);
        }
    }
}
