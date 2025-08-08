package nbbrd.heylogs.ext.gitlab;

import internal.heylogs.git.ThreeDotDiff;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import tests.heylogs.spi.ThreeDotDiffConverter;

import java.net.URL;
import java.util.Arrays;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabCompareLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabCompareLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/3e47abcfffc7388737ea671e3ee806968fc18417...ac9318b20caf0a7eb3927edac95d3344f7df10a7")));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "GitLabCompareLinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentable(String description, URL input, URL output, URL base, String namespace, String project,
                                  @ConvertWith(ThreeDotDiffConverter.class) ThreeDotDiff diff, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, GitLabCompareLink::getBase)
                    .returns(Arrays.asList(namespace.split("/", -1)), GitLabCompareLink::getNamespace)
                    .returns(project, GitLabCompareLink::getProject)
                    .returns(diff, GitLabCompareLink::getDiff)
                    .returns(output, GitLabCompareLink::toURL);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
//                    .withMessage(error)
            ;
        }
    }

    @Test
    public void testDerive() {
        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...v1.1.0")).derive("v2.0.0"))
                .returns(ThreeDotDiff.parse("v1.1.0...v2.0.0"), GitLabCompareLink::getDiff)
                .returns(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.1.0...v2.0.0"), GitLabCompareLink::toURL);

        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD")).derive("v2.0.0"))
                .returns(ThreeDotDiff.parse("v1.0.0...v2.0.0"), GitLabCompareLink::getDiff)
                .returns(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...v2.0.0"), GitLabCompareLink::toURL);

        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/HEAD...HEAD")).derive("v2.0.0"))
                .returns(ThreeDotDiff.parse("v2.0.0...v2.0.0"), GitLabCompareLink::getDiff)
                .returns(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v2.0.0...v2.0.0"), GitLabCompareLink::toURL);
    }
}