package nbbrd.heylogs.ext.forgejo;

import nbbrd.heylogs.spi.Forge;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeAssert.assertForgeCompliance;

class ForgejoTest {

    @Test
    void testCompliance() {
        assertForgeCompliance(new Forgejo());
    }

    @Test
    void testIsCompareLink() {
        Forge x = new Forgejo();
        assertThat(x.isCompareLink(urlOf("https://nbb.be"))).isFalse();
        assertThat(x.isCompareLink(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1"))).isTrue();
        assertThat(x.isCompareLink(urlOf("https://localhost:8080/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1"))).isFalse();
    }

    @Test
    void testGetProjectURL() {
        Forge x = new Forgejo();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getProjectURL(urlOf("https://nbb.be")));

        assertThat(x.getProjectURL(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1")))
                .isEqualTo(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge"));
    }
}