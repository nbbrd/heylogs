package nbbrd.heylogs.ext.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubRequestRef.*;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class GitHubRequestRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("#217"));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("#"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("heylogs#217"));

        assertThat(parse("#217"))
                .returns(null, GitHubRequestRef::getOwner)
                .returns(null, GitHubRequestRef::getRepo)
                .returns(217, GitHubRequestRef::getRequestNumber)
                .hasToString("#217");

        assertThat(parse("nbbrd/heylogs#217"))
                .returns("nbbrd", GitHubRequestRef::getOwner)
                .returns("heylogs", GitHubRequestRef::getRepo)
                .returns(217, GitHubRequestRef::getRequestNumber)
                .hasToString("nbbrd/heylogs#217");

        assertThat(parse("nbbRD/heyLOGS#217"))
                .describedAs("case sensitivity")
                .returns("nbbRD", GitHubRequestRef::getOwner)
                .returns("heyLOGS", GitHubRequestRef::getRepo)
                .returns(217, GitHubRequestRef::getRequestNumber)
                .hasToString("nbbRD/heyLOGS#217");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, Type.NUMBER));
        assertThatNullPointerException().isThrownBy(() -> of(pullRequest2170, (Type) null));

        assertThat(of(pullRequest217, Type.NUMBER).isCompatibleWith(pullRequest2170)).isFalse();
        assertThat(of(pullRequest217, Type.NUMBER).isCompatibleWith(pullRequest217)).isTrue();

        assertThat(of(pullRequest217, Type.OWNER_REPO_NUMBER).isCompatibleWith(pullRequest2170)).isFalse();
        assertThat(of(pullRequest217, Type.OWNER_REPO_NUMBER).isCompatibleWith(pullRequest217)).isTrue();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("#217").isCompatibleWith(pullRequest217)).isTrue();
        assertThat(parse("nbbrd/heylogs#217").isCompatibleWith(pullRequest217)).isTrue();
        assertThat(parse("jdemetra/jdplus-main#217").isCompatibleWith(pullRequest217)).isFalse();
        assertThat(parse("#217").isCompatibleWith(pullRequest2170)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("#217").getType()).isEqualTo(Type.NUMBER);
        assertThat(parse("nbbrd/heylogs#217").getType()).isEqualTo(Type.OWNER_REPO_NUMBER);
    }

    private final GitHubRequestLink pullRequest217 = GitHubRequestLink.parse(urlOf("https://github.com/nbbrd/heylogs/pull/217"));
    private final GitHubRequestLink pullRequest2170 = GitHubRequestLink.parse(urlOf("https://github.com/nbbrd/heylogs/pull/2170"));
}