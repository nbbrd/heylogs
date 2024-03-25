package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubIssueRef.*;
import static org.assertj.core.api.Assertions.*;

class GitHubIssueRefTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("#"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("heylogs#173"));

        assertThat(parse("#173"))
                .returns(null, GitHubIssueRef::getOwner)
                .returns(null, GitHubIssueRef::getRepo)
                .returns(173, GitHubIssueRef::getIssueNumber)
                .hasToString("#173");

        assertThat(parse("nbbrd/heylogs#173"))
                .returns("nbbrd", GitHubIssueRef::getOwner)
                .returns("heylogs", GitHubIssueRef::getRepo)
                .returns(173, GitHubIssueRef::getIssueNumber)
                .hasToString("nbbrd/heylogs#173");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> shortOf(null));

        assertThat(shortOf(issue173).isCompatibleWith(issue173)).isTrue();
        assertThat(shortOf(issue173).isCompatibleWith(pullRequest217)).isFalse();
        assertThat(shortOf(pullRequest217).isCompatibleWith(issue173)).isFalse();
        assertThat(shortOf(pullRequest217).isCompatibleWith(pullRequest217)).isTrue();

        assertThatNullPointerException().isThrownBy(() -> fullOf(null));

        assertThat(fullOf(issue173).isCompatibleWith(issue173)).isTrue();
        assertThat(fullOf(issue173).isCompatibleWith(pullRequest217)).isFalse();
        assertThat(fullOf(pullRequest217).isCompatibleWith(issue173)).isFalse();
        assertThat(fullOf(pullRequest217).isCompatibleWith(pullRequest217)).isTrue();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("#173").isCompatibleWith(issue173)).isTrue();
        assertThat(parse("nbbrd/heylogs#173").isCompatibleWith(issue173)).isTrue();
        assertThat(parse("jdemetra/jdplus-main#173").isCompatibleWith(issue173)).isFalse();
        assertThat(parse("#173").isCompatibleWith(pullRequest217)).isFalse();
    }

    @Test
    public void testIsShort() {
        assertThat(parse("#173").isShort()).isTrue();
        assertThat(parse("nbbrd/heylogs#173").isShort()).isFalse();
    }

    private final GitHubIssueLink issue173 = GitHubIssueLink.parse("https://github.com/nbbrd/heylogs/issues/173");
    private final GitHubIssueLink pullRequest217 = GitHubIssueLink.parse("https://github.com/nbbrd/heylogs/pull/217");
}