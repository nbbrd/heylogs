package nbbrd.heylogs.ext.pep440;

import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class Pep440Test {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new Pep440());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new Pep440();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull(""))
                .withMessage("PEP 440 does not take any arguments");

        assertThat(x.getVersioningPredicateOrNull(null))
                .accepts("1.0.0", "1.0", "2.1.3", "1.0.0a1", "1.0.0b2", "1.0.0rc1",
                        "1.0.0.post1", "1.0.0.dev3", "1!2.0.0", "21.3", "0.1")
                .rejects("", "abc", ".1.0", "1.0.0-beta", "v1.0.0");
    }

    @Test
    void testComparator() {
        Versioning x = new Pep440();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningComparatorOrNull(""))
                .withMessage("PEP 440 does not take any arguments");

        assertThat(x.getVersioningComparatorOrNull(null))
                .isNotNull()
                .satisfies(comparator -> {
                    // basic release ordering
                    assertThat(comparator.compare("2.0.0", "1.0.0")).isPositive();
                    assertThat(comparator.compare("1.0.0", "2.0.0")).isNegative();
                    assertThat(comparator.compare("1.0.0", "1.0.0")).isZero();
                    assertThat(comparator.compare("1.1.0", "1.0.0")).isPositive();

                    // pre-release ordering: dev < a < b < rc < final
                    assertThat(comparator.compare("1.0.0.dev1", "1.0.0a1")).isNegative();
                    assertThat(comparator.compare("1.0.0a1", "1.0.0b1")).isNegative();
                    assertThat(comparator.compare("1.0.0b1", "1.0.0rc1")).isNegative();
                    assertThat(comparator.compare("1.0.0rc1", "1.0.0")).isNegative();

                    // post-release ordering: final < post
                    assertThat(comparator.compare("1.0.0.post1", "1.0.0")).isPositive();
                    assertThat(comparator.compare("1.0.0.post2", "1.0.0.post1")).isPositive();

                    // epoch ordering
                    assertThat(comparator.compare("1!0.0.0", "999.999.999")).isPositive();

                    // incomparable
                    assertThat(comparator.compare("not-pep440", "1.0.0")).isZero();
                });
    }

    @Test
    void testFamilyMapper() {
        Versioning x = new Pep440();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningFamilyMapperOrNull(""))
                .withMessage("PEP 440 does not take any arguments");

        assertThat(x.getVersioningFamilyMapperOrNull(null))
                .isNotNull()
                .satisfies(mapper -> {
                    assertThat(mapper.apply("2.4.0")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.4.1")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.5.0")).isEqualTo("2.5");
                    assertThat(mapper.apply("3.0.0")).isEqualTo("3.0");
                    assertThat(mapper.apply("1.0.0a1")).isEqualTo("1.0");
                    assertThat(mapper.apply("1.0.0.post1")).isEqualTo("1.0");
                    assertThat(mapper.apply("21.3")).isEqualTo("21.3");
                    assertThat(mapper.apply("42")).isEqualTo("42");
                    assertThat(mapper.apply("not-pep440")).isNull();
                });
    }
}
