package nbbrd.heylogs.ext.forgejo;

import internal.heylogs.spi.URLExtractor;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.ext.forgejo.ForgejoIssueLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoIssueLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/5173"));
    }

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse("https://codeberg.org/Gadgetbridge/issues/5173"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse("https://codeberg.org/nbb%20rd/Gadgetbridge/issues/173"))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/issues/173"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/hey%20logs/issues/173"))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing type")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/173"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid type")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/isues/173"))
                .withMessage("Invalid path item: expecting [issues, pulls], found 'isues'");

        assertThatIllegalArgumentException()
                .describedAs("missing number")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/issues"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid number")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/"))
                .withMessage("Invalid path item at index 3: expecting pattern '\\d+', found ''");

        assertThat(parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/173"))
                .returns(URLExtractor.urlOf("https://codeberg.org"), ForgejoIssueLink::getBase)
                .returns("Freeyourgadget", ForgejoIssueLink::getOwner)
                .returns("Gadgetbridge", ForgejoIssueLink::getRepo)
                .returns("issues", ForgejoIssueLink::getType)
                .returns(173, ForgejoIssueLink::getIssueNumber)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/173");

        assertThat(parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/217"))
                .returns(URLExtractor.urlOf("https://codeberg.org"), ForgejoIssueLink::getBase)
                .returns("Freeyourgadget", ForgejoIssueLink::getOwner)
                .returns("Gadgetbridge", ForgejoIssueLink::getRepo)
                .returns("pulls", ForgejoIssueLink::getType)
                .returns(217, ForgejoIssueLink::getIssueNumber)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/217");

        assertThat(parse("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/issues/173"))
                .describedAs("case sensitivity")
                .returns(URLExtractor.urlOf("https://codeberg.org"), ForgejoIssueLink::getBase)
                .returns("FreeyourGADGET", ForgejoIssueLink::getOwner)
                .returns("GadgetBRIDGE", ForgejoIssueLink::getRepo)
                .returns("issues", ForgejoIssueLink::getType)
                .returns(173, ForgejoIssueLink::getIssueNumber)
                .hasToString("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/issues/173");

        assertThat(parse("https://localhost:8080/Freeyourgadget/Gadgetbridge/issues/173"))
                .returns(URLExtractor.urlOf("https://localhost:8080"), ForgejoIssueLink::getBase)
                .returns("Freeyourgadget", ForgejoIssueLink::getOwner)
                .returns("Gadgetbridge", ForgejoIssueLink::getRepo)
                .returns("issues", ForgejoIssueLink::getType)
                .returns(173, ForgejoIssueLink::getIssueNumber)
                .hasToString("https://localhost:8080/Freeyourgadget/Gadgetbridge/issues/173");
    }

}