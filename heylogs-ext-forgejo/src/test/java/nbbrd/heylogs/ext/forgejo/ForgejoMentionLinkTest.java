package nbbrd.heylogs.ext.forgejo;

import internal.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoMentionLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoMentionLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/orgs/nbbrd/teams/devs")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(urlOf("https://github.com/")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs")));

        assertThat(parse(urlOf("https://github.com/charphi")))
                .returns(URLExtractor.urlOf("https://github.com"), ForgejoMentionLink::getBase)
                .returns("charphi", ForgejoMentionLink::getUser)
                .returns(null, ForgejoMentionLink::getOrganization)
                .returns(null, ForgejoMentionLink::getTeamName)
                .hasToString("https://github.com/charphi");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(urlOf("https://github.com/orgs/nbbrd/teams")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(urlOf("https://github.com/orgs/nbbrd")));

        assertThat(parse(urlOf("https://github.com/orgs/nbbrd/teams/devs")))
                .returns(URLExtractor.urlOf("https://github.com"), ForgejoMentionLink::getBase)
                .returns(null, ForgejoMentionLink::getUser)
                .returns("nbbrd", ForgejoMentionLink::getOrganization)
                .returns("devs", ForgejoMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbrd/teams/devs");

        assertThat(parse(urlOf("https://github.com/orgs/nbbRD/teams/dEvs")))
                .describedAs("case sensitivity")
                .returns(URLExtractor.urlOf("https://github.com"), ForgejoMentionLink::getBase)
                .returns(null, ForgejoMentionLink::getUser)
                .returns("nbbRD", ForgejoMentionLink::getOrganization)
                .returns("dEvs", ForgejoMentionLink::getTeamName)
                .hasToString("https://github.com/orgs/nbbRD/teams/dEvs");
    }
}