package nbbrd.heylogs;

import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.DomainConfig.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DomainConfigTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .describedAs("Empty string")
                .isThrownBy(() -> parse(""))
                .withMessage("Cannot find ':' in ''");

        assertThatIllegalArgumentException()
                .describedAs("Not forge kebab case")
                .isThrownBy(() -> parse("domain:b oom"))
                .withMessage("Invalid forge ID 'b oom'");

        assertThatIllegalArgumentException()
                .describedAs("Not forge kebab case")
                .isThrownBy(() -> parse("domain:bOOm"))
                .withMessage("Invalid forge ID 'bOOm'");

        assertThatIllegalArgumentException()
                .describedAs("Invalid domain")
                .isThrownBy(() -> parse("b oom:hello"))
                .withMessageContaining("Invalid domain 'b oom'");

        assertThatIllegalArgumentException()
                .describedAs("Invalid domain")
                .isThrownBy(() -> parse("bo/om:hello"))
                .withMessageContaining("Invalid domain 'bo/om'");

        assertThat(parse("domain:hello"))
                .returns("domain", DomainConfig::getDomain)
                .returns("hello", DomainConfig::getForgeId)
                .hasToString("domain:hello");

        assertThat(parse("domain.nbb.be:hello"))
                .returns("domain.nbb.be", DomainConfig::getDomain)
                .returns("hello", DomainConfig::getForgeId)
                .hasToString("domain.nbb.be:hello");
    }
}