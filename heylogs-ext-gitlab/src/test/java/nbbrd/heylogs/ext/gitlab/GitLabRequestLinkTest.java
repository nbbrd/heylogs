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

    @Test
    void testResolve() {
        // Basic number reference
        assertThat(GitLabRequestLink.resolve(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab"), "!1"))
                .returns(urlOf("https://gitlab.com"), GitLabRequestLink::getBase)
                .returns(java.util.Arrays.asList("nbbrd"), GitLabRequestLink::getNamespace)
                .returns("heylogs-ext-gitlab", GitLabRequestLink::getProject)
                .returns(1, GitLabRequestLink::getNumber)
                .hasToString("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/merge_requests/1");

        // Namespace/project reference
        assertThat(GitLabRequestLink.resolve(urlOf("https://gitlab.com/otherns/otherproj"), "nbbrd/heylogs-ext-gitlab!1"))
                .returns(urlOf("https://gitlab.com"), GitLabRequestLink::getBase)
                .returns(java.util.Arrays.asList("otherns"), GitLabRequestLink::getNamespace)
                .returns("otherproj", GitLabRequestLink::getProject)
                .returns(1, GitLabRequestLink::getNumber)
                .hasToString("https://gitlab.com/otherns/otherproj/-/merge_requests/1");

        // Project URL with trailing slash
        assertThat(GitLabRequestLink.resolve(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/"), "!1"))
                .returns(urlOf("https://gitlab.com"), GitLabRequestLink::getBase)
                .returns(java.util.Arrays.asList("nbbrd"), GitLabRequestLink::getNamespace)
                .returns("heylogs-ext-gitlab", GitLabRequestLink::getProject)
                .returns(1, GitLabRequestLink::getNumber)
                .hasToString("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/merge_requests/1");
    }

}