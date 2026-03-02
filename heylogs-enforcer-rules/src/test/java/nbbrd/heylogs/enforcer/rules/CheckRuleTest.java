package nbbrd.heylogs.enforcer.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckRuleTest {

    @Test
    void testToString() {
        CheckRule rule = new CheckRule();
        assertThat(rule.toString()).contains("CheckChangelogRule").contains("inputFiles");
    }
}

