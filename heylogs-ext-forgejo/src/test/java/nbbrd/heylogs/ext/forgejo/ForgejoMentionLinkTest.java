package nbbrd.heylogs.ext.forgejo;

import internal.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.ext.forgejo.ForgejoMentionLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoMentionLinkTest {

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
                .returns(URLExtractor.urlOf("https://github.com"), ForgejoMentionLink::getBase)
                .returns("charphi", ForgejoMentionLink::getUser)
                .returns(null, ForgejoMentionLink::getOrganization)
                .returns(null, ForgejoMentionLink::getTeamName)
                .hasToString("https://github.com/charphi");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/orgs/nbbrd/teams"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/orgs/nbbrd"));

        assertThat(parse("https://github.com/orgs/nbbrd/teams/devs"))
                .returns(URLExtractor.urlOf("https://github.com"), ForgejoMentionLink::getBase)
                .returns(null, ForgejoMentionLink::getUser)
                .returns("nbbrd", ForgejoMentionLink::getOrganization)
                .returns("devs", ForgejoMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbrd/teams/devs");

        assertThat(parse("https://github.com/orgs/nbbRD/teams/dEvs"))
                .describedAs("case sensitivity")
                .returns(URLExtractor.urlOf("https://github.com"), ForgejoMentionLink::getBase)
                .returns(null, ForgejoMentionLink::getUser)
                .returns("nbbRD", ForgejoMentionLink::getOrganization)
                .returns("dEvs", ForgejoMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbRD/teams/dEvs");
    }

}