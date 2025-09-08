package nbbrd.heylogs.ext.calver;

import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class CalVerTest {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new CalVer());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new CalVer();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull(null))
                .withMessageContaining("Value is null");

        assertThat(x.getVersioningPredicateOrNull("YY.0M.MICRO"))
                .accepts("20.04")
                .accepts("20.04.1")
                .rejects("x20.04")
                .rejects("2020.04");
    }
}