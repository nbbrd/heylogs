package internal.heylogs.spi;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;

class URLExtractorTest {

    @Test
    void getPathArray() {
        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs"), true))
                .containsExactly("nbbrd", "heylogs");

        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs/"), true))
                .containsExactly("nbbrd", "heylogs");

        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs"), false))
                .containsExactly("nbbrd", "heylogs");

        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs/"), false))
                .containsExactly("nbbrd", "heylogs", "");
    }
}