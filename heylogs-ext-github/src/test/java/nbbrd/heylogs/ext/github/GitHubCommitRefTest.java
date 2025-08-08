package nbbrd.heylogs.ext.github;

import internal.heylogs.git.Hash;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubCommitRef.of;
import static nbbrd.heylogs.ext.github.GitHubCommitRef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class GitHubCommitRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("862157d"));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("#"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("heylogs#173"));

        assertThat(parse("862157d"))
                .returns(null, GitHubCommitRef::getOwner)
                .returns(null, GitHubCommitRef::getRepo)
                .returns(Hash.parse("862157d"), GitHubCommitRef::getHash)
                .hasToString("862157d");

        assertThat(parse("nbbrd@862157d"))
                .returns("nbbrd", GitHubCommitRef::getOwner)
                .returns(null, GitHubCommitRef::getRepo)
                .returns(Hash.parse("862157d"), GitHubCommitRef::getHash)
                .hasToString("nbbrd@862157d");

        assertThat(parse("nbbrd/heylogs@862157d"))
                .returns("nbbrd", GitHubCommitRef::getOwner)
                .returns("heylogs", GitHubCommitRef::getRepo)
                .returns(Hash.parse("862157d"), GitHubCommitRef::getHash)
                .hasToString("nbbrd/heylogs@862157d");

        assertThat(parse("nbbRD/heyLOGS@862157D"))
                .describedAs("case sensitivity")
                .returns("nbbRD", GitHubCommitRef::getOwner)
                .returns("heyLOGS", GitHubCommitRef::getRepo)
                .returns(Hash.parse("862157D"), GitHubCommitRef::getHash)
                .hasToString("nbbRD/heyLOGS@862157D");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, GitHubCommitRef.Type.HASH));
        assertThatNullPointerException().isThrownBy(() -> of(commit, null));

        assertThat(of(commit, GitHubCommitRef.Type.HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, GitHubCommitRef.Type.OWNER_HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, GitHubCommitRef.Type.OWNER_REPO_HASH).isCompatibleWith(commit)).isTrue();
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
        assertThat(parse("862157d").getType()).isEqualTo(GitHubCommitRef.Type.HASH);
        assertThat(parse("nbbrd@862157d").getType()).isEqualTo(GitHubCommitRef.Type.OWNER_HASH);
        assertThat(parse("nbbrd/heylogs@862157d").getType()).isEqualTo(GitHubCommitRef.Type.OWNER_REPO_HASH);
    }

    private final GitHubCommitLink commit = GitHubCommitLink.parse(urlOf("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"));
}