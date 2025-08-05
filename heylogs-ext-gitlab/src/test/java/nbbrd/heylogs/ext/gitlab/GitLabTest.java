package nbbrd.heylogs.ext.gitlab;

import nbbrd.heylogs.spi.Forge;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeAssert.assertForgeCompliance;

class GitLabTest {

    @Test
    void testCompliance() {
        assertForgeCompliance(new GitLab());
    }

    @Test
    void testIsCompareLink() {
        Forge x = new GitLab();
        assertThat(x.isCompareLink(urlOf("https://nbb.be"))).isFalse();
        assertThat(x.isCompareLink(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD"))).isTrue();
        assertThat(x.isCompareLink(urlOf("http://localhost:8080/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD"))).isFalse();
    }

    @Test
    void testGetProjectURL() {
        Forge x = new GitLab();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getProjectURL(urlOf("https://nbb.be")));

        assertThat(x.getProjectURL(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD")))
                .isEqualTo(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab"));
    }
}