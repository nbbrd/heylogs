package nbbrd.heylogs.ext.github;

import internal.heylogs.URLExtractor;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.ext.github.GitHubIssueLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubIssueLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://github.com/nbbrd/heylogs/issues/173"));
    }

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse("https://github.com/heylogs/issues/173"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse("https://github.com/nbb%20rd/heylogs/issues/173"))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse("https://github.com/nbbrd/issues/173"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse("https://github.com/nbbrd/hey%20logs/issues/173"))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing type")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/173"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid type")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/isues/173"))
                .withMessage("Invalid path item: expecting [issues, pull], found 'isues'");

        assertThatIllegalArgumentException()
                .describedAs("missing number")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/issues"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid number")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/issues/"))
                .withMessage("Invalid path item at index 3: expecting pattern '\\d+', found ''");

        assertThat(parse("https://github.com/nbbrd/heylogs/issues/173"))
                .returns(URLExtractor.urlOf("https://github.com"), GitHubIssueLink::getBase)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("issues", GitHubIssueLink::getType)
                .returns(173, GitHubIssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/issues/173");

        assertThat(parse("https://github.com/nbbrd/heylogs/pull/217"))
                .returns(URLExtractor.urlOf("https://github.com"), GitHubIssueLink::getBase)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("pull", GitHubIssueLink::getType)
                .returns(217, GitHubIssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/pull/217");


        assertThat(parse("https://localhost:8080/nbbrd/heylogs/issues/173"))
                .returns(URLExtractor.urlOf("https://localhost:8080"), GitHubIssueLink::getBase)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("issues", GitHubIssueLink::getType)
                .returns(173, GitHubIssueLink::getIssueNumber)
                .hasToString("https://localhost:8080/nbbrd/heylogs/issues/173");
    }

}