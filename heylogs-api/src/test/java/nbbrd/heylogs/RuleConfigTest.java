package nbbrd.heylogs;

import nbbrd.heylogs.spi.RuleSeverity;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.RuleConfig.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RuleConfigTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .describedAs("Empty string")
                .isThrownBy(() -> parse(""));

        assertThatIllegalArgumentException()
                .describedAs("Not kebab case")
                .isThrownBy(() -> parse("b oom"));

        assertThatIllegalArgumentException()
                .describedAs("Not kebab case")
                .isThrownBy(() -> parse("bOOm"));

        assertThat(parse("hello"))
                .returns("hello", RuleConfig::getId)
                .returns(null, RuleConfig::getSeverity)
                .hasToString("hello");

        assertThatIllegalArgumentException()
                .describedAs("Invalid severity")
                .isThrownBy(() -> parse("hello:"));

        assertThatIllegalArgumentException()
                .describedAs("Invalid severity")
                .isThrownBy(() -> parse("hello:boom"));

        assertThat(parse("hello:WARN"))
                .returns("hello", RuleConfig::getId)
                .returns(RuleSeverity.WARN, RuleConfig::getSeverity)
                .hasToString("hello:WARN");
    }
}