package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Scan;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static internal.heylogs.maven.plugin.HeylogsParameters.*;
import static java.util.Collections.singletonList;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class ScanMojo extends HeylogsMojo {

    @Parameter(defaultValue = WORKING_DIR_CHANGELOG, property = INPUT_FILE_PROPERTY)
    private File inputFile;

    @Parameter(defaultValue = MOJO_LOG_FILE, property = OUTPUT_FILE_PROPERTY)
    private File outputFile;

    @Parameter(defaultValue = StylishFormat.ID, property = FORMAT_ID_PROPERTY)
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Scanning has been skipped.");
            return;
        }

        if (inputFile.exists()) {
            scan();
        } else {
            if (isRootProject()) {
                raiseErrorMissingChangelog();
            } else {
                notifyMissingChangelog();
            }
        }
    }

    private void scan() throws MojoExecutionException {
        Heylogs heylogs = initHeylogs(false);

        Scan scan = Scan
                .builder()
                .source(inputFile.toString())
                .summary(heylogs.scan(readChangelog(inputFile)))
                .build();

        try (Writer writer = newWriter(outputFile, getLog()::info)) {
            heylogs.formatStatus(formatId, writer, singletonList(scan));
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }
    }
}
