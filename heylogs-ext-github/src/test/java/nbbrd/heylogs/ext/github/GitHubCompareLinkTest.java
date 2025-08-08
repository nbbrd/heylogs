package nbbrd.heylogs.ext.github;

import internal.heylogs.git.ThreeDotDiff;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubCompareLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubCompareLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing OID")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/compare")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid OID")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...")))
                .withMessage("Invalid path item at index 3: expecting pattern '(.+)\\.{3}(.+)', found 'v0.7.2...'");

        assertThatIllegalArgumentException()
                .describedAs("missing compare")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/v0.7.2...HEAD")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid compare")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/compar/v0.7.2...HEAD")))
                .withMessage("Invalid path item: expecting [compare], found 'compar'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/compare/v0.7.2...HEAD")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/hey%20logs/compare/v0.7.2...HEAD")))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse(urlOf("https://github.com/heylogs/compare/v0.7.2...HEAD")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbb%20rd/heylogs/compare/v0.7.2...HEAD")))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD")))
                .returns(urlOf("https://github.com"), GitHubCompareLink::getBase)
                .returns("nbbrd", GitHubCompareLink::getOwner)
                .returns("heylogs", GitHubCompareLink::getRepo)
                .returns(ThreeDotDiff.parse("v0.7.2...HEAD"), GitHubCompareLink::getDiff)
                .hasToString("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD");

        assertThat(parse(urlOf("https://github.com/nbbRD/heyLOGS/compare/v0.7.2...HEAD")))
                .describedAs("case sensitivity")
                .returns(urlOf("https://github.com"), GitHubCompareLink::getBase)
                .returns("nbbRD", GitHubCompareLink::getOwner)
                .returns("heyLOGS", GitHubCompareLink::getRepo)
                .returns(ThreeDotDiff.parse("v0.7.2...HEAD"), GitHubCompareLink::getDiff)
                .hasToString("https://github.com/nbbRD/heyLOGS/compare/v0.7.2...HEAD");

        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/compare/v0.7.2...HEAD")))
                .returns(urlOf("https://localhost:8080"), GitHubCompareLink::getBase)
                .returns("nbbrd", GitHubCompareLink::getOwner)
                .returns("heylogs", GitHubCompareLink::getRepo)
                .returns(ThreeDotDiff.parse("v0.7.2...HEAD"), GitHubCompareLink::getDiff)
                .hasToString("https://localhost:8080/nbbrd/heylogs/compare/v0.7.2...HEAD");
    }

    @Test
    public void testDerive() {
        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/compare/v1.0.0...v1.1.0")).derive("v2.0.0"))
                .returns(ThreeDotDiff.parse("v1.1.0...v2.0.0"), GitHubCompareLink::getDiff)
                .returns(urlOf("https://github.com/nbbrd/heylogs/compare/v1.1.0...v2.0.0"), GitHubCompareLink::toURL);

        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/compare/v1.0.0...HEAD")).derive("v2.0.0"))
                .returns(ThreeDotDiff.parse("v1.0.0...v2.0.0"), GitHubCompareLink::getDiff)
                .returns(urlOf("https://github.com/nbbrd/heylogs/compare/v1.0.0...v2.0.0"), GitHubCompareLink::toURL);

        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/compare/HEAD...HEAD")).derive("v2.0.0"))
                .returns(ThreeDotDiff.parse("v2.0.0...v2.0.0"), GitHubCompareLink::getDiff)
                .returns(urlOf("https://github.com/nbbrd/heylogs/compare/v2.0.0...v2.0.0"), GitHubCompareLink::toURL);
    }
}