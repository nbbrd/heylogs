package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.net.URL;
import java.util.Arrays;

import static nbbrd.heylogs.ext.gitlab.GitLabIssueLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabIssueLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/issues/1"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabIssueLinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, String output, URL base, String namespace, String project, int number, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, GitLabIssueLink::getBase)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabIssueLink::getNamespace)
                    .returns(project, GitLabIssueLink::getProject)
                    .returns(number, GitLabIssueLink::getNumber)
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