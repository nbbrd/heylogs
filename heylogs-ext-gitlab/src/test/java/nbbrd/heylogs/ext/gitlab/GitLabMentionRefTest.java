package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;

import static nbbrd.heylogs.ext.gitlab.GitLabMentionRef.of;
import static nbbrd.heylogs.ext.gitlab.GitLabMentionRef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class GitLabMentionRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("@charphi"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabIssueRefExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, String output, String namespace, String project, int number, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(GitLabIssueRef.parse(input))
                    .describedAs(description)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabIssueRef::getNamespace)
                    .returns(project, GitLabIssueRef::getProject)
                    .returns(number, GitLabIssueRef::getNumber)
                    .hasToString(output);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> GitLabIssueRef.parse(input))
//                    .withMessage(error)
            ;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null));

        assertThat(of(charphi).isCompatibleWith(charphi)).isTrue();
        assertThat(of(charphi).isCompatibleWith(subgroup)).isFalse();
        assertThat(of(subgroup).isCompatibleWith(charphi)).isFalse();
        assertThat(of(subgroup).isCompatibleWith(subgroup)).isTrue();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("@charphi").isCompatibleWith(charphi)).isTrue();
        assertThat(parse("@charphi").isCompatibleWith(user)).isFalse();
        assertThat(parse("@charphi").isCompatibleWith(subgroup)).isFalse();

        assertThat(parse("@nbbrd/heylogs/ext").isCompatibleWith(subgroup)).isTrue();
        assertThat(parse("@nbbrd/heylogs/ext").isCompatibleWith(user)).isFalse();
        assertThat(parse("@nbbrd/heylogs/ext").isCompatibleWith(user)).isFalse();
    }

    private final GitLabMentionLink charphi = GitLabMentionLink.parse("https://gitlab.com/charphi");
    private final GitLabMentionLink user = GitLabMentionLink.parse("https://gitlab.com/user");
    private final GitLabMentionLink subgroup = GitLabMentionLink.parse("https://gitlab.com/nbbrd/heylogs/ext");
}