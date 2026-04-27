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

    @Test
    void testComparator() {
        Versioning x = new SemVer();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningComparatorOrNull(""))
                .withMessage("Semver does not take any arguments");

        assertThat(x.getVersioningComparatorOrNull(null))
                .isNotNull()
                .satisfies(comparator -> {
                    assertThat(comparator.compare("2.0.0", "1.0.0")).isPositive();
                    assertThat(comparator.compare("1.0.0", "2.0.0")).isNegative();
                    assertThat(comparator.compare("1.0.0", "1.0.0")).isZero();
                    assertThat(comparator.compare("2.4.1", "2.4.0")).isPositive();
                    assertThat(comparator.compare("not-semver", "1.0.0")).isZero(); // incomparable
                });
    }

    @Test
    void testFamilyMapper() {
        Versioning x = new SemVer();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningFamilyMapperOrNull(""))
                .withMessage("Semver does not take any arguments");

        assertThat(x.getVersioningFamilyMapperOrNull(null))
                .isNotNull()
                .satisfies(mapper -> {
                    assertThat(mapper.apply("2.4.0")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.4.1")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.5.0")).isEqualTo("2.5");
                    assertThat(mapper.apply("3.0.0")).isEqualTo("3.0");
                    assertThat(mapper.apply("not-semver")).isNull();
                });
    }
}