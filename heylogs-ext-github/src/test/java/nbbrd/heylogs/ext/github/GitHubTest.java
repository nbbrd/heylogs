package nbbrd.heylogs.ext.github;

import nbbrd.heylogs.spi.Forge;
import org.junit.jupiter.api.Test;

import static internal.heylogs.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeAssert.assertForgeCompliance;

class GitHubTest {

    @Test
    void testCompliance() {
        assertForgeCompliance(new GitHub());
    }

    @Test
    void testIsCompareLink() {
        Forge x = new GitHub();
        assertThat(x.isCompareLink(urlOf("https://nbb.be"))).isFalse();
        assertThat(x.isCompareLink(urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD"))).isTrue();
    }

    @Test
    void testGetProjectURL() {
        Forge x = new GitHub();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getProjectURL(urlOf("https://nbb.be")));

        assertThat(x.getProjectURL(urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD")))
                .isEqualTo(urlOf("https://github.com/nbbrd/heylogs"));
    }
}