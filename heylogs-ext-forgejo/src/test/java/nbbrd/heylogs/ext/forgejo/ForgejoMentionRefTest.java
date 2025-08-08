package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoMentionRef.of;
import static nbbrd.heylogs.ext.forgejo.ForgejoMentionRef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class ForgejoMentionRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("@charphi"));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("@"));

        assertThat(parse("@charphi"))
                .returns("charphi", ForgejoMentionRef::getUser)
                .returns(null, ForgejoMentionRef::getOrganization)
                .returns(null, ForgejoMentionRef::getTeamName)
                .hasToString("@charphi");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("@nbbrd/devs/stuff"));

        assertThat(parse("@nbbrd/devs"))
                .returns(null, ForgejoMentionRef::getUser)
                .returns("nbbrd", ForgejoMentionRef::getOrganization)
                .returns("devs", ForgejoMentionRef::getTeamName)
                .hasToString("@nbbrd/devs");

        assertThat(parse("@nbbRD/dEvs"))
                .describedAs("case sensitivity")
                .returns(null, ForgejoMentionRef::getUser)
                .returns("nbbRD", ForgejoMentionRef::getOrganization)
                .returns("dEvs", ForgejoMentionRef::getTeamName)
                .hasToString("@nbbRD/dEvs");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null));

        assertThat(of(charphi).isCompatibleWith(charphi)).isTrue();
        assertThat(of(charphi).isCompatibleWith(devs)).isFalse();
        assertThat(of(devs).isCompatibleWith(charphi)).isFalse();
        assertThat(of(devs).isCompatibleWith(devs)).isTrue();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("@charphi").isCompatibleWith(charphi)).isTrue();
        assertThat(parse("@charphi").isCompatibleWith(user)).isFalse();
        assertThat(parse("@charphi").isCompatibleWith(devs)).isFalse();
        assertThat(parse("@charphi").isCompatibleWith(team)).isFalse();

        assertThat(parse("@nbbrd/devs").isCompatibleWith(devs)).isTrue();
        assertThat(parse("@nbbrd/devs").isCompatibleWith(team)).isFalse();
        assertThat(parse("@nbbrd/devs").isCompatibleWith(user)).isFalse();
        assertThat(parse("@nbbrd/devs").isCompatibleWith(user)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("@charphi").getType()).isEqualTo(ForgejoMentionRef.Type.USER);
        assertThat(parse("@nbbrd/devs").getType()).isEqualTo(ForgejoMentionRef.Type.TEAM);
    }

    private final ForgejoMentionLink charphi = ForgejoMentionLink.parse(urlOf("https://github.com/charphi"));
    private final ForgejoMentionLink user = ForgejoMentionLink.parse(urlOf("https://github.com/user"));
    private final ForgejoMentionLink devs = ForgejoMentionLink.parse(urlOf("https://github.com/orgs/nbbrd/teams/devs"));
    private final ForgejoMentionLink team = ForgejoMentionLink.parse(urlOf("https://github.com/orgs/nbbrd/teams/team"));
}