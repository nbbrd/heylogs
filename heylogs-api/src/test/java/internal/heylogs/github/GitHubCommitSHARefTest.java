package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubCommitSHARef.of;
import static internal.heylogs.github.GitHubCommitSHARef.parse;
import static org.assertj.core.api.Assertions.*;

class GitHubCommitSHARefTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("#"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("heylogs#173"));

        assertThat(parse("862157d"))
                .returns(null, GitHubCommitSHARef::getOwner)
                .returns(null, GitHubCommitSHARef::getRepo)
                .returns("862157d", GitHubCommitSHARef::getHash)
                .hasToString("862157d");

        assertThat(parse("nbbrd@862157d"))
                .returns("nbbrd", GitHubCommitSHARef::getOwner)
                .returns(null, GitHubCommitSHARef::getRepo)
                .returns("862157d", GitHubCommitSHARef::getHash)
                .hasToString("nbbrd@862157d");

        assertThat(parse("nbbrd/heylogs@862157d"))
                .returns("nbbrd", GitHubCommitSHARef::getOwner)
                .returns("heylogs", GitHubCommitSHARef::getRepo)
                .returns("862157d", GitHubCommitSHARef::getHash)
                .hasToString("nbbrd/heylogs@862157d");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, GitHubCommitSHARef.Type.HASH));
        assertThatNullPointerException().isThrownBy(() -> of(commit, null));

        assertThat(of(commit, GitHubCommitSHARef.Type.HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, GitHubCommitSHARef.Type.OWNER_HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, GitHubCommitSHARef.Type.OWNER_REPO_HASH).isCompatibleWith(commit)).isTrue();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testIsCompatibleWith() {
        assertThatNullPointerException().isThrownBy(() -> parse("862157d").isCompatibleWith(null));

        assertThat(parse("862157d").isCompatibleWith(commit)).isTrue();
        assertThat(parse("000007d").isCompatibleWith(commit)).isFalse();

        assertThat(parse("nbbrd@862157d").isCompatibleWith(commit)).isTrue();
        assertThat(parse("nbbrd@000007d").isCompatibleWith(commit)).isFalse();
        assertThat(parse("abcde@862157d").isCompatibleWith(commit)).isFalse();

        assertThat(parse("nbbrd/heylogs@862157d").isCompatibleWith(commit)).isTrue();
        assertThat(parse("nbbrd/heylogs@000007d").isCompatibleWith(commit)).isFalse();
        assertThat(parse("abcde/heylogs@862157d").isCompatibleWith(commit)).isFalse();
        assertThat(parse("nbbrd/abcdefg@862157d").isCompatibleWith(commit)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("862157d").getType()).isEqualTo(GitHubCommitSHARef.Type.HASH);
        assertThat(parse("nbbrd@862157d").getType()).isEqualTo(GitHubCommitSHARef.Type.OWNER_HASH);
        assertThat(parse("nbbrd/heylogs@862157d").getType()).isEqualTo(GitHubCommitSHARef.Type.OWNER_REPO_HASH);
    }

    private final GitHubCommitSHALink commit = GitHubCommitSHALink.parse("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");
}