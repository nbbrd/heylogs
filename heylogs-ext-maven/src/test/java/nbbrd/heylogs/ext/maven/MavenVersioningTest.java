package nbbrd.heylogs.ext.maven;

import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class MavenVersioningTest {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new MavenVersioning());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new MavenVersioning();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull(""))
                .withMessage("Maven versioning does not take any arguments");

        assertThat(x.getVersioningPredicateOrNull(null))
                .accepts("1.0.0", "1.0", "2.1.3", "1.0.0-SNAPSHOT", "2.1.0.Final",
                        "3.0.0.M1", "1.0.0-beta-1", "1.0.0-alpha-2", "1.0.0-RC1")
                .rejects("", "abc", ".1.0", "1", "v1.0.0");
    }

    @Test
    void testComparator() {
        Versioning x = new MavenVersioning();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningComparatorOrNull(""))
                .withMessage("Maven versioning does not take any arguments");

        assertThat(x.getVersioningComparatorOrNull(null))
                .isNotNull()
                .satisfies(comparator -> {
                    // basic release ordering
                    assertThat(comparator.compare("2.0.0", "1.0.0")).isPositive();
                    assertThat(comparator.compare("1.0.0", "2.0.0")).isNegative();
                    assertThat(comparator.compare("1.0.0", "1.0.0")).isZero();
                    assertThat(comparator.compare("1.1.0", "1.0.0")).isPositive();

                    // qualifier ordering: alpha < beta < rc < (release) < sp
                    assertThat(comparator.compare("1.0.0-alpha-1", "1.0.0-beta-1")).isNegative();
                    assertThat(comparator.compare("1.0.0-beta-1", "1.0.0-RC1")).isNegative();
                    assertThat(comparator.compare("1.0.0-RC1", "1.0.0")).isNegative();

                    // SNAPSHOT sorts before release
                    assertThat(comparator.compare("1.0.0-SNAPSHOT", "1.0.0")).isNegative();

                    // incomparable
                    assertThat(comparator.compare("not-maven", "1.0.0")).isZero();
                });
    }

    @Test
    void testFamilyMapper() {
        Versioning x = new MavenVersioning();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningFamilyMapperOrNull(""))
                .withMessage("Maven versioning does not take any arguments");

        assertThat(x.getVersioningFamilyMapperOrNull(null))
                .isNotNull()
                .satisfies(mapper -> {
                    assertThat(mapper.apply("2.4.0")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.4.1")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.5.0")).isEqualTo("2.5");
                    assertThat(mapper.apply("3.0.0")).isEqualTo("3.0");
                    assertThat(mapper.apply("1.0.0-SNAPSHOT")).isEqualTo("1.0");
                    assertThat(mapper.apply("2.1.0.Final")).isEqualTo("2.1");
                    assertThat(mapper.apply("not-maven")).isNull();
                });
    }
}
