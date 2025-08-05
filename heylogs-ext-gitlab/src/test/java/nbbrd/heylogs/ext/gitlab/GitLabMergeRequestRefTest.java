package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabMergeRequestRef.of;
import static nbbrd.heylogs.ext.gitlab.GitLabMergeRequestRef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class GitLabMergeRequestRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("!1"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabMergeRequestRefExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentable(String description, String input, String output, String namespace, String project, int number, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabMergeRequestRef::getNamespace)
                    .returns(project, GitLabMergeRequestRef::getProject)
                    .returns(number, GitLabMergeRequestRef::getNumber)
                    .hasToString(output);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
//                    .withMessage(error)
            ;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, GitLabRefType.SAME_PROJECT));
        assertThatNullPointerException().isThrownBy(() -> of(link, null));

        assertThat(of(link, GitLabRefType.SAME_PROJECT).isCompatibleWith(link)).isTrue();
        assertThat(of(link, GitLabRefType.SAME_NAMESPACE).isCompatibleWith(link)).isTrue();
        assertThat(of(link, GitLabRefType.CROSS_PROJECT).isCompatibleWith(link)).isTrue();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testIsCompatibleWith() {
        assertThatNullPointerException().isThrownBy(() -> parse("!1").isCompatibleWith(null));

        assertThat(parse("!1").isCompatibleWith(link)).isTrue();
        assertThat(parse("!0").isCompatibleWith(link)).isFalse();

        assertThat(parse("heylogs-ext-gitlab!1").isCompatibleWith(link)).isTrue();
        assertThat(parse("heylogs-ext-gitlab!0").isCompatibleWith(link)).isFalse();
        assertThat(parse("abcde!1").isCompatibleWith(link)).isFalse();

        assertThat(parse("nbbrd/heylogs-ext-gitlab!1").isCompatibleWith(link)).isTrue();
        assertThat(parse("nbbrd/heylogs-ext-gitlab!0").isCompatibleWith(link)).isFalse();
        assertThat(parse("abcde/heylogs-ext-gitlab!1").isCompatibleWith(link)).isFalse();
        assertThat(parse("nbbrd/abcdefg!1").isCompatibleWith(link)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("!1").getType()).isEqualTo(GitLabRefType.SAME_PROJECT);
        assertThat(parse("heylogs-ext-gitlab!1").getType()).isEqualTo(GitLabRefType.SAME_NAMESPACE);
        assertThat(parse("nbbrd/heylogs-ext-gitlab!1").getType()).isEqualTo(GitLabRefType.CROSS_PROJECT);
    }

    private final GitLabMergeRequestLink link = GitLabMergeRequestLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/merge_requests/1"));
}