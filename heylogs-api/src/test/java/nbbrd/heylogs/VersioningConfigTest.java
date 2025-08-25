package nbbrd.heylogs;

import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.VersioningConfig.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class VersioningConfigTest {

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
                .returns("hello", VersioningConfig::getId)
                .returns(null, VersioningConfig::getArg)
                .hasToString("hello");

        assertThat(parse("hello:"))
                .returns("hello", VersioningConfig::getId)
                .returns("", VersioningConfig::getArg)
                .hasToString("hello:");

        assertThat(parse("hello:world"))
                .returns("hello", VersioningConfig::getId)
                .returns("world", VersioningConfig::getArg)
                .hasToString("hello:world");
    }
}