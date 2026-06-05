package nbbrd.heylogs.cli;

import _test.ShadedJarLauncher;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class MainCommandIT {

    @Test
    public void testShadedJar() throws IOException, URISyntaxException {

        ShadedJarLauncher shadedJar = ShadedJarLauncher.of(MainCommandIT.class, fileName -> fileName.startsWith("heylogs-cli-") && fileName.endsWith("-bin.jar"));

        assertThat(shadedJar.readString("--version"))
                .contains("heylogs")
                .doesNotContainIgnoringCase("warning");
    }
}
