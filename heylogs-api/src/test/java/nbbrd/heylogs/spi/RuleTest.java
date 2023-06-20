package nbbrd.heylogs.spi;

import nbbrd.heylogs.spi.Rule;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static nbbrd.heylogs.spi.Rule.isEnabled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class RuleTest {

    @Test
    public void testIsEnabled() {
        Properties properties = new Properties();

        assertThatNullPointerException().isThrownBy(() -> isEnabled(properties, null));

        assertThat(isEnabled(properties, "")).isFalse();

        assertThat(isEnabled(properties, "abc")).isFalse();
        assertThat(isEnabled(properties, "xyz")).isFalse();

        properties.setProperty(Rule.ENABLE_KEY, "abc");
        assertThat(isEnabled(properties, "abc")).isTrue();
        assertThat(isEnabled(properties, "xyz")).isFalse();

        properties.setProperty(Rule.ENABLE_KEY, "abc,xyz");
        assertThat(isEnabled(properties, "abc")).isTrue();
        assertThat(isEnabled(properties, "xyz")).isTrue();
    }
}
