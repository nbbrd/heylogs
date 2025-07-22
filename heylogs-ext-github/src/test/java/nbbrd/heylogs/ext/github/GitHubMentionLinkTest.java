package nbbrd.heylogs.ext.github;

import internal.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.ext.github.GitHubMentionLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubMentionLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://github.com/orgs/nbbrd/teams/devs"));
    }

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs"));

        assertThat(parse("https://github.com/charphi"))
                .returns(URLExtractor.urlOf("https://github.com"), GitHubMentionLink::getBase)
                .returns("charphi", GitHubMentionLink::getUser)
                .returns(null, GitHubMentionLink::getOrganization)
                .returns(null, GitHubMentionLink::getTeamName)
                .hasToString("https://github.com/charphi");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/orgs/nbbrd/teams"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/orgs/nbbrd"));

        assertThat(parse("https://github.com/orgs/nbbrd/teams/devs"))
                .returns(URLExtractor.urlOf("https://github.com"), GitHubMentionLink::getBase)
                .returns(null, GitHubMentionLink::getUser)
                .returns("nbbrd", GitHubMentionLink::getOrganization)
                .returns("devs", GitHubMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbrd/teams/devs");

        assertThat(parse("https://github.com/orgs/nbbRD/teams/dEvs"))
                .describedAs("case sensitivity")
                .returns(URLExtractor.urlOf("https://github.com"), GitHubMentionLink::getBase)
                .returns(null, GitHubMentionLink::getUser)
                .returns("nbbRD", GitHubMentionLink::getOrganization)
                .returns("dEvs", GitHubMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbRD/teams/dEvs");
    }

}