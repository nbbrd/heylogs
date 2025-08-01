package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.net.URL;
import java.util.Arrays;

import static nbbrd.heylogs.ext.gitlab.GitLabCommitLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabCommitLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabCommitLinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, String output, URL base, String namespace, String project, String hash, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, GitLabCommitLink::getBase)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabCommitLink::getNamespace)
                    .returns(project, GitLabCommitLink::getProject)
                    .returns(hash, GitLabCommitLink::getHash)
                    .hasToString(output);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
//                    .withMessage(error)
            ;
        }
    }
}