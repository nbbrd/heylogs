package internal.heylogs.base;

import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.VersioningAssert;

import static org.assertj.core.api.Assertions.*;

class BaseVersioningsTest {

    @Test
    public void testCompliance() {
        new BaseVersionings().getProviders()
                .forEach(VersioningAssert::assertVersioningCompliance);
    }

    @Test
    public void testRegexVersioning() {
        Versioning x = BaseVersionings.REGEX_VERSIONING;

        assertThat(x.getVersioningArgValidator().apply(null))
                .isEqualTo("Value is null");

        assertThat(x.getVersioningArgValidator().apply(""))
                .isNull();

        assertThat(x.getVersioningArgValidator().apply("\\d+"))
                .isNull();

        assertThat(x.getVersioningArgValidator().apply("(\\d+"))
                .startsWith("Value is invalid: Unclosed group near index 4");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull(null))
                .withMessageContaining("Value is null");

        assertThatCode(() -> x.getVersioningPredicateOrNull(""))
                .doesNotThrowAnyException();

        assertThatCode(() -> x.getVersioningPredicateOrNull("\\d+"))
                .doesNotThrowAnyException();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull("(\\d+"))
                .withMessageContaining("Unclosed group near index 4");

        assertThat(x.getVersioningPredicateOrNull("\\d+"))
                .accepts("123")
                .rejects("")
                .rejects("abc");
    }
}