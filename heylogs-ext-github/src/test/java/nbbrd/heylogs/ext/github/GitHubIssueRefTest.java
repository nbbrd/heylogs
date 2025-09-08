package nbbrd.heylogs.ext.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubIssueRef.*;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class GitHubIssueRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("#173"));
    }

    @Test
    public void testRepresentable() {
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

        assertThat(parse("nbbRD/heyLOGS#173"))
                .describedAs("case sensitivity")
                .returns("nbbRD", GitHubIssueRef::getOwner)
                .returns("heyLOGS", GitHubIssueRef::getRepo)
                .returns(173, GitHubIssueRef::getIssueNumber)
                .hasToString("nbbRD/heyLOGS#173");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, Type.NUMBER));
        assertThatNullPointerException().isThrownBy(() -> of(issue173, (Type) null));

        assertThat(of(issue173, Type.NUMBER).isCompatibleWith(issue173)).isTrue();
        assertThat(of(issue173, Type.NUMBER).isCompatibleWith(issue1730)).isFalse();

        assertThat(of(issue173, Type.OWNER_REPO_NUMBER).isCompatibleWith(issue173)).isTrue();
        assertThat(of(issue173, Type.OWNER_REPO_NUMBER).isCompatibleWith(issue1730)).isFalse();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("#173").isCompatibleWith(issue173)).isTrue();
        assertThat(parse("nbbrd/heylogs#173").isCompatibleWith(issue173)).isTrue();
        assertThat(parse("jdemetra/jdplus-main#173").isCompatibleWith(issue173)).isFalse();
        assertThat(parse("#173").isCompatibleWith(issue1730)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("#173").getType()).isEqualTo(Type.NUMBER);
        assertThat(parse("nbbrd/heylogs#173").getType()).isEqualTo(Type.OWNER_REPO_NUMBER);
    }

    private final GitHubIssueLink issue173 = GitHubIssueLink.parse(urlOf("https://github.com/nbbrd/heylogs/issues/173"));
    private final GitHubIssueLink issue1730 = GitHubIssueLink.parse(urlOf("https://github.com/nbbrd/heylogs/issues/1730"));
}