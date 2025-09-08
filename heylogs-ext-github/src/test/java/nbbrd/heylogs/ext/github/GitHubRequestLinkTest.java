package nbbrd.heylogs.ext.github;

import internal.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubRequestLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubRequestLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/nbbrd/heylogs/pull/217")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse(urlOf("https://github.com/heylogs/pull/217")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbb%20rd/heylogs/pull/217")))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/pull/217")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/hey%20logs/pull/217")))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing type")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/217")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid type")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/isues/217")))
                .withMessage("Invalid path item: expecting [pull], found 'isues'");

        assertThatIllegalArgumentException()
                .describedAs("missing number")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/pull")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid number")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/pull/")))
                .withMessage("Invalid path item at index 3: expecting pattern '\\d+', found ''");

        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/pull/217")))
                .returns(URLExtractor.urlOf("https://github.com"), GitHubRequestLink::getBase)
                .returns("nbbrd", GitHubRequestLink::getOwner)
                .returns("heylogs", GitHubRequestLink::getRepo)
                .returns(217, GitHubRequestLink::getRequestNumber)
                .hasToString("https://github.com/nbbrd/heylogs/pull/217");

        assertThat(parse(urlOf("https://github.com/nbbRD/heyLOGS/pull/217")))
                .describedAs("case sensitivity")
                .returns(URLExtractor.urlOf("https://github.com"), GitHubRequestLink::getBase)
                .returns("nbbRD", GitHubRequestLink::getOwner)
                .returns("heyLOGS", GitHubRequestLink::getRepo)
                .returns(217, GitHubRequestLink::getRequestNumber)
                .hasToString("https://github.com/nbbRD/heyLOGS/pull/217");

        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/pull/217")))
                .returns(URLExtractor.urlOf("https://localhost:8080"), GitHubRequestLink::getBase)
                .returns("nbbrd", GitHubRequestLink::getOwner)
                .returns("heylogs", GitHubRequestLink::getRepo)
                .returns(217, GitHubRequestLink::getRequestNumber)
                .hasToString("https://localhost:8080/nbbrd/heylogs/pull/217");
    }
}