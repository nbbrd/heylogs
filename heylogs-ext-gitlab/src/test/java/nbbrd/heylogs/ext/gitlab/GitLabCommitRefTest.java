package nbbrd.heylogs.ext.gitlab;

import internal.heylogs.git.Hash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import tests.heylogs.spi.HashConverter;

import java.util.Arrays;

import static internal.heylogs.spi.URLExtractor.urlOf;
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
    public void testRepresentable(String description, String input, String output, String namespace, String project,
                                  @ConvertWith(HashConverter.class) Hash hash, String error) {
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
        assertThatNullPointerException().isThrownBy(() -> of(null, GitLabRefType.SAME_PROJECT));
        assertThatNullPointerException().isThrownBy(() -> of(link, (GitLabRefType) null));

        assertThat(of(link, GitLabRefType.SAME_PROJECT).isCompatibleWith(link)).isTrue();
        assertThat(of(link, GitLabRefType.SAME_NAMESPACE).isCompatibleWith(link)).isTrue();
        assertThat(of(link, GitLabRefType.CROSS_PROJECT).isCompatibleWith(link)).isTrue();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testIsCompatibleWith() {
        assertThatNullPointerException().isThrownBy(() -> parse("656ad7d").isCompatibleWith(null));

        assertThat(parse("656ad7d").isCompatibleWith(link)).isTrue();
        assertThat(parse("00000000").isCompatibleWith(link)).isFalse();

        assertThat(parse("heylogs-ext-gitlab@656ad7d").isCompatibleWith(link)).isTrue();
        assertThat(parse("heylogs-ext-gitlab@00000000").isCompatibleWith(link)).isFalse();
        assertThat(parse("abcde@656ad7d").isCompatibleWith(link)).isFalse();

        assertThat(parse("nbbrd/heylogs-ext-gitlab@656ad7d").isCompatibleWith(link)).isTrue();
        assertThat(parse("nbbrd/heylogs-ext-gitlab@00000000").isCompatibleWith(link)).isFalse();
        assertThat(parse("abcde/heylogs-ext-gitlab@656ad7d").isCompatibleWith(link)).isFalse();
        assertThat(parse("nbbrd/abcdefg@656ad7d").isCompatibleWith(link)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("656ad7d").getType()).isEqualTo(GitLabRefType.SAME_PROJECT);
        assertThat(parse("heylogs-ext-gitlab@656ad7d").getType()).isEqualTo(GitLabRefType.SAME_NAMESPACE);
        assertThat(parse("nbbrd/heylogs-ext-gitlab@656ad7d").getType()).isEqualTo(GitLabRefType.CROSS_PROJECT);
    }

    private final GitLabCommitLink link = GitLabCommitLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac"));
}