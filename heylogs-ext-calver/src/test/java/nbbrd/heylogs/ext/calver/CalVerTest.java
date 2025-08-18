package nbbrd.heylogs.ext.calver;

import nbbrd.heylogs.Config;
import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class CalVerTest {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new CalVer());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new CalVer();

        assertThat(x.isValidVersion("20.04", Config.DEFAULT)).isFalse();
        assertThat(x.isValidVersion("20.04.1", Config.DEFAULT)).isFalse();

        Config ubuntu = Config.builder().versioningArg("YY.0M.MICRO").build();

        assertThat(x.isValidVersion("20.04", ubuntu)).isTrue();
        assertThat(x.isValidVersion("20.04.1", ubuntu)).isTrue();
    }
}