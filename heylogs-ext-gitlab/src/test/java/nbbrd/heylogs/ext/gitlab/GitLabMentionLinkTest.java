package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.net.URL;
import java.util.Arrays;

import static nbbrd.heylogs.ext.gitlab.GitLabMentionLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabMentionLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://github.com/orgs/nbbrd/teams/devs"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabMentionLinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, String output, URL base, String namespace, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, GitLabMentionLink::getBase)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabMentionLink::getNamespace)
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