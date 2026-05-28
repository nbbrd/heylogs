package nbbrd.heylogs.ext.github;

import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubTagLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubTagLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/rjdverse/rjd3xjars/releases/tag/v3.8.2")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing tag value")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/releases/tag")))
                .withMessage("Invalid path length: expecting [5], found 4");

        assertThatIllegalArgumentException()
                .describedAs("invalid tag keyword")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/releases/notag/v1.0.0")))
                .withMessage("Invalid path item: expecting [tag], found 'notag'");

        assertThatIllegalArgumentException()
                .describedAs("missing releases keyword")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/tag/v1.0.0")))
                .withMessage("Invalid path length: expecting [5], found 4");

        assertThat(parse(urlOf("https://github.com/rjdverse/rjd3xjars/releases/tag/v3.8.2")))
                .returns(urlOf("https://github.com"), GitHubTagLink::getBase)
                .returns("rjdverse", GitHubTagLink::getOwner)
                .returns("rjd3xjars", GitHubTagLink::getRepo)
                .returns("v3.8.2", GitHubTagLink::getTag)
                .hasToString("https://github.com/rjdverse/rjd3xjars/releases/tag/v3.8.2");

        assertThat(parse(urlOf("https://github.com/olivierlacan/keep-a-changelog/releases/tag/v0.0.1")))
                .returns("v0.0.1", GitHubTagLink::getTag)
                .hasToString("https://github.com/olivierlacan/keep-a-changelog/releases/tag/v0.0.1");

        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/releases/tag/v1.0.0")))
                .returns(urlOf("https://localhost:8080"), GitHubTagLink::getBase)
                .returns("nbbrd", GitHubTagLink::getOwner)
                .returns("heylogs", GitHubTagLink::getRepo)
                .returns("v1.0.0", GitHubTagLink::getTag)
                .hasToString("https://localhost:8080/nbbrd/heylogs/releases/tag/v1.0.0");
    }

    @Test
    public void testToRef() {
        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/releases/tag/v1.0.0")).toRef(null))
                .isNull();
    }
}


