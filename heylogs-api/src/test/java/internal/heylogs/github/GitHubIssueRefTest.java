package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubIssueRef.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

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

    @Test
    public void testIsCompatibleWith() {
        GitHubIssueLink issue173 = GitHubIssueLink.parse("https://github.com/nbbrd/heylogs/issues/173");
        GitHubIssueLink pullRequest217 = GitHubIssueLink.parse("https://github.com/nbbrd/heylogs/pull/217");

        assertThat(parse("#173").isCompatibleWith(issue173)).isTrue();
        assertThat(parse("nbbrd/heylogs#173").isCompatibleWith(issue173)).isTrue();
        assertThat(parse("jdemetra/jdplus-main#173").isCompatibleWith(issue173)).isFalse();
        assertThat(parse("#173").isCompatibleWith(pullRequest217)).isFalse();
    }
}