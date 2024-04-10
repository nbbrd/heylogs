package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubMentionRef.of;
import static internal.heylogs.github.GitHubMentionRef.parse;
import static org.assertj.core.api.Assertions.*;

class GitHubMentionRefTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("@"));

        assertThat(parse("@charphi"))
                .returns("charphi", GitHubMentionRef::getUser)
                .returns(null, GitHubMentionRef::getOrganization)
                .returns(null, GitHubMentionRef::getTeamName)
                .hasToString("@charphi");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("@nbbrd/devs/stuff"));

        assertThat(parse("@nbbrd/devs"))
                .returns(null, GitHubMentionRef::getUser)
                .returns("nbbrd", GitHubMentionRef::getOrganization)
                .returns("devs", GitHubMentionRef::getTeamName)
                .hasToString("@nbbrd/devs");
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
        assertThat(parse("@charphi").getType()).isEqualTo(GitHubMentionRef.Type.USER);
        assertThat(parse("@nbbrd/devs").getType()).isEqualTo(GitHubMentionRef.Type.TEAM);
    }

    private final GitHubMentionLink charphi = GitHubMentionLink.parse("https://github.com/charphi");
    private final GitHubMentionLink user = GitHubMentionLink.parse("https://github.com/user");
    private final GitHubMentionLink devs = GitHubMentionLink.parse("https://github.com/orgs/nbbrd/teams/devs");
    private final GitHubMentionLink team = GitHubMentionLink.parse("https://github.com/orgs/nbbrd/teams/team");
}