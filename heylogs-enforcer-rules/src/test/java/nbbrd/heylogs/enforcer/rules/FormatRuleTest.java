package nbbrd.heylogs.enforcer.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatRuleTest {

    @Test
    void testToString() {
        FormatRule rule = new FormatRule();
        assertThat(rule.toString()).contains("FormatChangelogRule").contains("inputFiles");
    }
}

