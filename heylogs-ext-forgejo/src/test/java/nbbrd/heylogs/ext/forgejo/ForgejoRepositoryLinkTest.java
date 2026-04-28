package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoRepositoryLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoRepositoryLinkTest {

    @Test
    void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://codeberg.org/nbbrd/heylogs")));
    }

    @Test
    void testValidProject() {
        ForgejoRepositoryLink link = parse(urlOf("https://codeberg.org/nbbrd/heylogs"));
        assertThat(link.getOwner()).isEqualTo("nbbrd");
        assertThat(link.getRepo()).isEqualTo("heylogs");
        assertThat(link.toURL().toString()).isEqualTo("https://codeberg.org/nbbrd/heylogs");

        ForgejoRepositoryLink linkWithTrailingSlash = parse(urlOf("https://codeberg.org/nbbrd/heylogs/"));
        assertThat(linkWithTrailingSlash.getOwner()).isEqualTo("nbbrd");
        assertThat(linkWithTrailingSlash.getRepo()).isEqualTo("heylogs");
        assertThat(linkWithTrailingSlash.toURL().toString()).isEqualTo("https://codeberg.org/nbbrd/heylogs");
    }

    @Test
    void testInvalidProject() {
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://codeberg.org/nbbrd")));
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://codeberg.org/nbb rd/heylogs")));
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://codeberg.org/nbbrd/hey logs")));
    }
}

