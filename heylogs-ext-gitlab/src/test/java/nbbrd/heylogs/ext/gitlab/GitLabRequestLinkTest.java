package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.net.URL;
import java.util.Arrays;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabRequestLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabRequestLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/merge_requests/1")));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabMergeRequestLinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentable(String description, URL input, URL output, URL base, String namespace, String project, int number, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, GitLabRequestLink::getBase)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabRequestLink::getNamespace)
                    .returns(project, GitLabRequestLink::getProject)
                    .returns(number, GitLabRequestLink::getNumber)
                    .returns(output, GitLabRequestLink::toURL);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
//                    .withMessage(error)
            ;
        }
    }

}