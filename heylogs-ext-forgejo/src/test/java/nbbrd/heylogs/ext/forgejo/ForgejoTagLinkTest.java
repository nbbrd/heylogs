package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoTagLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoTagLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/releases/tag/0.86.1")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing tag value")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/nbbrd/heylogs/releases/tag")))
                .withMessage("Invalid path length: expecting [5], found 4");

        assertThatIllegalArgumentException()
                .describedAs("invalid tag keyword")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/nbbrd/heylogs/releases/notag/v1.0.0")))
                .withMessage("Invalid path item: expecting [tag], found 'notag'");

        assertThat(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/releases/tag/0.86.1")))
                .returns(urlOf("https://codeberg.org"), ForgejoTagLink::getBase)
                .returns("Freeyourgadget", ForgejoTagLink::getOwner)
                .returns("Gadgetbridge", ForgejoTagLink::getRepo)
                .returns("0.86.1", ForgejoTagLink::getBranchName)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/releases/tag/0.86.1");

        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/releases/tag/v1.0.0")))
                .returns(urlOf("https://localhost:8080"), ForgejoTagLink::getBase)
                .returns("nbbrd", ForgejoTagLink::getOwner)
                .returns("heylogs", ForgejoTagLink::getRepo)
                .returns("v1.0.0", ForgejoTagLink::getBranchName)
                .hasToString("https://localhost:8080/nbbrd/heylogs/releases/tag/v1.0.0");
    }

    @Test
    public void testToRef() {
        assertThat(parse(urlOf("https://codeberg.org/nbbrd/heylogs/releases/tag/v1.0.0")).toRef(null))
                .isNull();
    }
}


