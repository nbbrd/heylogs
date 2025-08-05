package nbbrd.heylogs.ext.forgejo;

import internal.heylogs.git.ThreeDotDiff;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoCompareLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoCompareLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing OID")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid OID")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...")))
                .withMessage("Invalid path item at index 3: expecting pattern '(.+)\\.{3}(.+)', found '0.86.0...'");

        assertThatIllegalArgumentException()
                .describedAs("missing compare")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/0.86.0...0.86.1")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid compare")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compar/0.86.0...0.86.1")))
                .withMessage("Invalid path item: expecting [compare], found 'compar'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/compare/0.86.0...0.86.1")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/hey%20logs/compare/0.86.0...0.86.1")))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Gadgetbridge/compare/0.86.0...0.86.1")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/nbb%20rd/Gadgetbridge/compare/0.86.0...0.86.1")))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThat(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1")))
                .returns(urlOf("https://codeberg.org"), ForgejoCompareLink::getBase)
                .returns("Freeyourgadget", ForgejoCompareLink::getOwner)
                .returns("Gadgetbridge", ForgejoCompareLink::getRepo)
                .returns(ThreeDotDiff.parse("0.86.0...0.86.1"), ForgejoCompareLink::getDiff)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1");

        assertThat(parse(urlOf("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/compare/0.86.0...0.86.1")))
                .describedAs("case sensitivity")
                .returns(urlOf("https://codeberg.org"), ForgejoCompareLink::getBase)
                .returns("FreeyourGADGET", ForgejoCompareLink::getOwner)
                .returns("GadgetBRIDGE", ForgejoCompareLink::getRepo)
                .returns(ThreeDotDiff.parse("0.86.0...0.86.1"), ForgejoCompareLink::getDiff)
                .hasToString("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/compare/0.86.0...0.86.1");

        assertThat(parse(urlOf("https://localhost:8080/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1")))
                .returns(urlOf("https://localhost:8080"), ForgejoCompareLink::getBase)
                .returns("Freeyourgadget", ForgejoCompareLink::getOwner)
                .returns("Gadgetbridge", ForgejoCompareLink::getRepo)
                .returns(ThreeDotDiff.parse("0.86.0...0.86.1"), ForgejoCompareLink::getDiff)
                .hasToString("https://localhost:8080/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1");
    }

}