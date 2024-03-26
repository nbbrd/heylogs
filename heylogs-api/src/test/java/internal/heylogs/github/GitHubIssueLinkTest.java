package internal.heylogs.github;

import internal.heylogs.GitHostLink;
import org.junit.jupiter.api.Test;

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
                .returns(GitHostLink.urlOf("https://github.com"), GitHubIssueLink::getBase)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("issues", GitHubIssueLink::getType)
                .returns(173, GitHubIssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/issues/173");

        assertThat(parse("https://github.com/nbbrd/heylogs/pull/217"))
                .returns(GitHostLink.urlOf("https://github.com"), GitHubIssueLink::getBase)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("pull", GitHubIssueLink::getType)
                .returns(217, GitHubIssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/pull/217");


        assertThat(parse("https://localhost:8080/nbbrd/heylogs/issues/173"))
                .returns(GitHostLink.urlOf("https://localhost:8080"), GitHubIssueLink::getBase)
                .returns("nbbrd", GitHubIssueLink::getOwner)
                .returns("heylogs", GitHubIssueLink::getRepo)
                .returns("issues", GitHubIssueLink::getType)
                .returns(173, GitHubIssueLink::getIssueNumber)
                .hasToString("https://localhost:8080/nbbrd/heylogs/issues/173");
    }

}