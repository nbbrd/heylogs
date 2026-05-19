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

    @Test
    void testComparator() {
        Versioning x = new CalVer();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningComparatorOrNull(null))
                .withMessageContaining("Value is null");

        assertThat(x.getVersioningComparatorOrNull("YYYY.MM.MICRO"))
                .isNotNull()
                .satisfies(comparator -> {
                    assertThat(comparator.compare("2024.03.1", "2024.03.0")).isPositive();
                    assertThat(comparator.compare("2024.03.0", "2024.03.1")).isNegative();
                    assertThat(comparator.compare("2024.03.1", "2024.03.1")).isZero();
                    assertThat(comparator.compare("2024.04.0", "2024.03.1")).isPositive();
                    assertThat(comparator.compare("2025.01.0", "2024.12.1")).isPositive();
                    assertThat(comparator.compare("invalid", "2024.03.1")).isZero(); // incomparable
                });
    }

    @Test
    void testFamilyMapper() {
        Versioning x = new CalVer();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningFamilyMapperOrNull(null))
                .withMessageContaining("Value is null");

        assertThat(x.getVersioningFamilyMapperOrNull("YYYY.MM.MICRO"))
                .isNotNull()
                .satisfies(mapper -> {
                    assertThat(mapper.apply("2024.03.0")).isEqualTo("2024.3");
                    assertThat(mapper.apply("2024.03.1")).isEqualTo("2024.3");
                    assertThat(mapper.apply("2024.04.0")).isEqualTo("2024.4");
                    assertThat(mapper.apply("2025.01.0")).isEqualTo("2025.1");
                    assertThat(mapper.apply("invalid")).isNull();
                });

        assertThat(x.getVersioningFamilyMapperOrNull("YY.0M.MINOR.MICRO"))
                .isNotNull()
                .satisfies(mapper -> {
                    assertThat(mapper.apply("24.03.0.1")).isEqualTo("24.3");
                    assertThat(mapper.apply("24.03.1.2")).isEqualTo("24.3");
                });
    }
}