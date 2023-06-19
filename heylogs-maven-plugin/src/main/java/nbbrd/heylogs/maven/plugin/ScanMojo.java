package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Scanner;
import nbbrd.heylogs.Status;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ScanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.input.file")
    private File inputFile;

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    private boolean skip;

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
            scanFile();
        } else {
            if (isRootProject()) {
                raiseErrorMissingFile();
            } else {
                notifyMissingFile();
            }
        }
    }

    private void scanFile() throws MojoExecutionException {
        try {
            getLog().info("Reading " + inputFile);
            Document changelog = read();

            Scanner scanner = getScanner();

            Status status = scanner.scan(changelog);
            StringBuilder text = new StringBuilder();
            scanner.formatStatus(text, inputFile.toString(), status);
            new BufferedReader(new StringReader(text.toString())).lines().forEach(getLog()::info);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while scanning changelog", ex);
        }
    }

    private Scanner getScanner() {
        return Scanner.ofServiceLoader()
                .toBuilder()
                .formatId(formatId)
                .build();
    }

    private boolean isRootProject() {
        File parentDir = projectBaseDir.getParentFile();
        if (parentDir != null) {
            File parentPom = new File(parentDir, "pom.xml");
            return !parentPom.exists();
        }
        return true;
    }

    private void raiseErrorMissingFile() throws MojoExecutionException {
        getLog().error("Missing changelog");
        throw new MojoExecutionException("Missing changelog");
    }

    private void notifyMissingFile() {
        getLog().info("Changelog not found");
    }

    public Document read() throws IOException {
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(inputFile.toPath())) {
            return parser.parseReader(reader);
        }
    }
}
