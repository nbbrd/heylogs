package nbbrd.heylogs.ext.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubRepositoryLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubRepositoryLinkTest {

    @Test
    void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/nbbrd/heylogs")));
    }

    @Test
    void testValidProject() {
        GitHubRepositoryLink link = parse(urlOf("https://github.com/nbbrd/heylogs"));
        assertThat(link.getOwner()).isEqualTo("nbbrd");
        assertThat(link.getRepo()).isEqualTo("heylogs");
        assertThat(link.toURL().toString()).isEqualTo("https://github.com/nbbrd/heylogs");

        GitHubRepositoryLink linkWithTrailingSlash = parse(urlOf("https://github.com/nbbrd/heylogs/"));
        assertThat(linkWithTrailingSlash.getOwner()).isEqualTo("nbbrd");
        assertThat(linkWithTrailingSlash.getRepo()).isEqualTo("heylogs");
        assertThat(linkWithTrailingSlash.toURL().toString()).isEqualTo("https://github.com/nbbrd/heylogs");
    }

    @Test
    void testInvalidProject() {
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://github.com/nbbrd")));
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://github.com/nbb rd/heylogs")));
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/hey logs")));
    }
}

