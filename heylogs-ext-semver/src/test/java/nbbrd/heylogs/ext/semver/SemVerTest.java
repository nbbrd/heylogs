package nbbrd.heylogs.ext.semver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class SemVerTest {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new SemVer());
    }

    @Test
    void isValidVersion() {
        SemVer x = new SemVer();
        assertThat(x.isValidVersion("1.1.0")).isTrue();
        assertThat(x.isValidVersion(".1.0")).isFalse();
    }
}