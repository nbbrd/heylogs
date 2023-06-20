package nbbrd.heylogs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AboutTest {

    @Test
    public void testVersion() {
        assertThat(About.VERSION)
                .isEqualTo("unknown");
    }
}
