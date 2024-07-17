package internal.heylogs.github;

import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeLoader;
import org.junit.jupiter.api.Test;

import static internal.heylogs.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class GitHubTest {

    @Test
    void testGetForgeId() {
        assertThat(new GitHub().getForgeId())
                .matches(ForgeLoader.ID_PATTERN);
    }

    @Test
    void testGetForgeName() {
        assertThat(new GitHub().getForgeName())
                .isNotBlank();
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