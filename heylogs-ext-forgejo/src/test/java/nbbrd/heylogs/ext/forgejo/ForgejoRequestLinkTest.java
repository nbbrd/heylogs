package nbbrd.heylogs.ext.forgejo;

import internal.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoRequestLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoRequestLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/5217")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Gadgetbridge/pulls/5217")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/nbb%20rd/Gadgetbridge/pulls/217")))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/pulls/217")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/hey%20logs/pulls/217")))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing type")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/217")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid type")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/isues/217")))
                .withMessage("Invalid path item: expecting [pulls], found 'isues'");

        assertThatIllegalArgumentException()
                .describedAs("missing number")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid number")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/")))
                .withMessage("Invalid path item at index 3: expecting pattern '\\d+', found ''");

        assertThat(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/217")))
                .returns(URLExtractor.urlOf("https://codeberg.org"), ForgejoRequestLink::getBase)
                .returns("Freeyourgadget", ForgejoRequestLink::getOwner)
                .returns("Gadgetbridge", ForgejoRequestLink::getRepo)
                .returns(217, ForgejoRequestLink::getIssueNumber)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/217");

        assertThat(parse(urlOf("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/pulls/217")))
                .describedAs("case sensitivity")
                .returns(URLExtractor.urlOf("https://codeberg.org"), ForgejoRequestLink::getBase)
                .returns("FreeyourGADGET", ForgejoRequestLink::getOwner)
                .returns("GadgetBRIDGE", ForgejoRequestLink::getRepo)
                .returns(217, ForgejoRequestLink::getIssueNumber)
                .hasToString("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/pulls/217");

        assertThat(parse(urlOf("https://localhost:8080/Freeyourgadget/Gadgetbridge/pulls/217")))
                .returns(URLExtractor.urlOf("https://localhost:8080"), ForgejoRequestLink::getBase)
                .returns("Freeyourgadget", ForgejoRequestLink::getOwner)
                .returns("Gadgetbridge", ForgejoRequestLink::getRepo)
                .returns(217, ForgejoRequestLink::getIssueNumber)
                .hasToString("https://localhost:8080/Freeyourgadget/Gadgetbridge/pulls/217");
    }

}