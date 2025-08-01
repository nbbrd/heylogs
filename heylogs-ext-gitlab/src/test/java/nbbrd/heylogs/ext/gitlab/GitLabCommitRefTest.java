package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;

import static nbbrd.heylogs.ext.gitlab.GitLabCommitRef.of;
import static nbbrd.heylogs.ext.gitlab.GitLabCommitRef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class GitLabCommitRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("862157d"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabCommitRefExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, String output, String namespace, String project, String hash, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(GitLabCommitRef.parse(input))
                    .describedAs(description)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabCommitRef::getNamespace)
                    .returns(project, GitLabCommitRef::getProject)
                    .returns(hash, GitLabCommitRef::getHash)
                    .hasToString(output);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> GitLabCommitRef.parse(input))
//                    .withMessage(error)
            ;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, GitLabCommitRef.Type.HASH));
        assertThatNullPointerException().isThrownBy(() -> of(commit, null));

        assertThat(of(commit, GitLabCommitRef.Type.HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, GitLabCommitRef.Type.SAME_NAMESPACE).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, GitLabCommitRef.Type.CROSS_PROJECT).isCompatibleWith(commit)).isTrue();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testIsCompatibleWith() {
        assertThatNullPointerException().isThrownBy(() -> parse("656ad7d").isCompatibleWith(null));

        assertThat(parse("656ad7d").isCompatibleWith(commit)).isTrue();
        assertThat(parse("00000000").isCompatibleWith(commit)).isFalse();

        assertThat(parse("heylogs-ext-gitlab@656ad7d").isCompatibleWith(commit)).isTrue();
        assertThat(parse("heylogs-ext-gitlab@00000000").isCompatibleWith(commit)).isFalse();
        assertThat(parse("abcde@656ad7d").isCompatibleWith(commit)).isFalse();

        assertThat(parse("nbbrd/heylogs-ext-gitlab@656ad7d").isCompatibleWith(commit)).isTrue();
        assertThat(parse("nbbrd/heylogs-ext-gitlab@00000000").isCompatibleWith(commit)).isFalse();
        assertThat(parse("abcde/heylogs-ext-gitlab@656ad7d").isCompatibleWith(commit)).isFalse();
        assertThat(parse("nbbrd/abcdefg@656ad7d").isCompatibleWith(commit)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("656ad7d").getType()).isEqualTo(GitLabCommitRef.Type.HASH);
        assertThat(parse("heylogs-ext-gitlab@656ad7d").getType()).isEqualTo(GitLabCommitRef.Type.SAME_NAMESPACE);
        assertThat(parse("nbbrd/heylogs-ext-gitlab@656ad7d").getType()).isEqualTo(GitLabCommitRef.Type.CROSS_PROJECT);
    }

    private final GitLabCommitLink commit = GitLabCommitLink.parse("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac");
}