package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.net.URL;
import java.util.Arrays;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabIssueLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabIssueLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/issues/1")));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabIssueLinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentable(String description, URL input, URL output, URL base, String namespace, String project, int number, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, GitLabIssueLink::getBase)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabIssueLink::getNamespace)
                    .returns(project, GitLabIssueLink::getProject)
                    .returns(number, GitLabIssueLink::getNumber)
                    .returns(output, GitLabIssueLink::toURL);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
//                    .withMessage(error)
            ;
        }
    }

    @Test
    void testResolve() {
        // Basic number reference
        assertThat(GitLabIssueLink.resolve(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab"), "#1"))
                .returns(urlOf("https://gitlab.com"), GitLabIssueLink::getBase)
                .returns(java.util.Arrays.asList("nbbrd"), GitLabIssueLink::getNamespace)
                .returns("heylogs-ext-gitlab", GitLabIssueLink::getProject)
                .returns(1, GitLabIssueLink::getNumber)
                .hasToString("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/issues/1");

        // Namespace/project reference
        assertThat(GitLabIssueLink.resolve(urlOf("https://gitlab.com/otherns/otherproj"), "nbbrd/heylogs-ext-gitlab#1"))
                .returns(urlOf("https://gitlab.com"), GitLabIssueLink::getBase)
                .returns(java.util.Arrays.asList("otherns"), GitLabIssueLink::getNamespace)
                .returns("otherproj", GitLabIssueLink::getProject)
                .returns(1, GitLabIssueLink::getNumber)
                .hasToString("https://gitlab.com/otherns/otherproj/-/issues/1");

        // Project URL with trailing slash
        assertThat(GitLabIssueLink.resolve(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/"), "#1"))
                .returns(urlOf("https://gitlab.com"), GitLabIssueLink::getBase)
                .returns(java.util.Arrays.asList("nbbrd"), GitLabIssueLink::getNamespace)
                .returns("heylogs-ext-gitlab", GitLabIssueLink::getProject)
                .returns(1, GitLabIssueLink::getNumber)
                .hasToString("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/issues/1");
    }

}