package internal.heylogs.spi;

import nbbrd.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;

class URLExtractorTest {

    @Test
    void getPathArray() {
        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs"), true))
                .containsExactly("nbbrd", "heylogs");
        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs"), false))
                .containsExactly("nbbrd", "heylogs");

        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs/"), true))
                .containsExactly("nbbrd", "heylogs");
        assertThat(URLExtractor.getPathArray(urlOf("https://github.com/nbbrd/heylogs/"), false))
                .containsExactly("nbbrd", "heylogs", "");

        assertThat(URLExtractor.getPathArray(urlOf("https://academicpages.github.io"), true))
                .containsExactly();
        assertThat(URLExtractor.getPathArray(urlOf("https://academicpages.github.io"), false))
                .containsExactly();

        assertThat(URLExtractor.getPathArray(urlOf("https://academicpages.github.io/"), true))
                .containsExactly();
        assertThat(URLExtractor.getPathArray(urlOf("https://academicpages.github.io/"), false))
                .containsExactly("");
    }
}