package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubMentionLink.NO_PORT;
import static internal.heylogs.github.GitHubMentionLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class GitHubMentionLinkTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs"));

        assertThat(parse("https://github.com/charphi"))
                .returns("https", GitHubMentionLink::getProtocol)
                .returns("github.com", GitHubMentionLink::getHost)
                .returns(NO_PORT, GitHubMentionLink::getPort)
                .returns("charphi", GitHubMentionLink::getUser)
                .returns(null, GitHubMentionLink::getOrganization)
                .returns(null, GitHubMentionLink::getTeamName)
                .hasToString("https://github.com/charphi");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/orgs/nbbrd/teams"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/orgs/nbbrd"));

        assertThat(parse("https://github.com/orgs/nbbrd/teams/devs"))
                .returns("https", GitHubMentionLink::getProtocol)
                .returns("github.com", GitHubMentionLink::getHost)
                .returns(NO_PORT, GitHubMentionLink::getPort)
                .returns(null, GitHubMentionLink::getUser)
                .returns("nbbrd", GitHubMentionLink::getOrganization)
                .returns("devs", GitHubMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbrd/teams/devs");
    }

}