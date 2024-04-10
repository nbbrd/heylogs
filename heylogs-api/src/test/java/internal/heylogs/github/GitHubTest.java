package internal.heylogs.github;

import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(x.isCompareLink("")).isFalse();
        assertThat(x.isCompareLink("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD")).isTrue();
    }
}