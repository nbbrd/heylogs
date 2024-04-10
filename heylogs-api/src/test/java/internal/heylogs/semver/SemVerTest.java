package internal.heylogs.semver;

import nbbrd.heylogs.spi.VersioningLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemVerTest {

    @Test
    void getVersioningId() {
        assertThat(new SemVer().getVersioningId())
                .matches(VersioningLoader.ID_PATTERN);
    }

    @Test
    void getVersioningName() {
        assertThat(new SemVer().getVersioningName())
                .isNotBlank();
    }

    @Test
    void isValidVersion() {
        SemVer x = new SemVer();
        assertThat(x.isValidVersion("1.1.0")).isTrue();
        assertThat(x.isValidVersion(".1.0")).isFalse();
    }
}