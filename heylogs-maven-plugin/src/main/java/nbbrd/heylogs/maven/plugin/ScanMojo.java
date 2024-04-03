package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

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
        Document changelog = readChangelog(inputFile);
        log(appendable -> heylogs.formatStatus(formatId, appendable, inputFile.toString(), heylogs.scan(changelog)), getLog()::info);
    }
}
