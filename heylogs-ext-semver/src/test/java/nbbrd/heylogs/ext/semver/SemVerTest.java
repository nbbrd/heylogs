package nbbrd.heylogs.ext.semver;

import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class SemVerTest {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new SemVer());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new SemVer();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull(""))
                .withMessage("Semver does not take any arguments");

        assertThat(x.getVersioningPredicateOrNull(null))
                .accepts("1.1.0")
                .rejects(".1.0");
    }
}