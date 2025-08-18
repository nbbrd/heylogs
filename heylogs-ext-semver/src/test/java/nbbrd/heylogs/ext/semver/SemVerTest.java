package nbbrd.heylogs.ext.semver;

import nbbrd.heylogs.Config;
import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class SemVerTest {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new SemVer());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new SemVer();
        assertThat(x.isValidVersion("1.1.0", Config.DEFAULT)).isTrue();
        assertThat(x.isValidVersion(".1.0", Config.DEFAULT)).isFalse();
    }
}