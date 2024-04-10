package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Scan;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

import static java.util.Collections.singletonList;

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
            scan();
        } else {
            if (isRootProject(projectBaseDir)) {
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
        log(appendable -> heylogs.formatStatus(formatId, appendable, singletonList(scan)), getLog()::info);
    }
}
