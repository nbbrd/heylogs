package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubIssueLink.NO_PORT;
import static internal.heylogs.github.GitHubIssueLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class GitHubIssueLinkTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/issues/"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/173"));

        assertThat(parse("https://github.com/nbbrd/heylogs/issues/173"))
                .returns("https", GitHubIssueLink::getProtocol)
                .returns("github.com", GitHubIssueLink::getHost)
                .returns(NO_PORT, GitHubIssueLink::getPort)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("issues", GitHubIssueLink::getType)
                .returns(173, GitHubIssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/issues/173");

        assertThat(parse("https://github.com/nbbrd/heylogs/pull/217"))
                .returns("https", GitHubIssueLink::getProtocol)
                .returns("github.com", GitHubIssueLink::getHost)
                .returns(NO_PORT, GitHubIssueLink::getPort)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("pull", GitHubIssueLink::getType)
                .returns(217, GitHubIssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/pull/217");


        assertThat(parse("https://localhost:8080/nbbrd/heylogs/issues/173"))
                .returns("https", GitHubIssueLink::getProtocol)
                .returns("localhost", GitHubIssueLink::getHost)
                .returns(8080, GitHubIssueLink::getPort)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("issues", GitHubIssueLink::getType)
                .returns(173, GitHubIssueLink::getIssueNumber)
                .hasToString("https://localhost:8080/nbbrd/heylogs/issues/173");
    }

}