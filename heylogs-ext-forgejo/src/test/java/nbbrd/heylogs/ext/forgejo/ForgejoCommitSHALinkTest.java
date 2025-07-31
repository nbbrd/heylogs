package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoCommitSHALink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoCommitSHALinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"));
    }

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .describedAs("missing hash")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid hash")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40"))
                .withMessage("Invalid path item at index 3: expecting pattern '[0-9a-f]{6,40}', found 'b5d40'");

        assertThatIllegalArgumentException()
                .describedAs("missing commit")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid commit")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/comit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .withMessage("Invalid path item: expecting [commit], found 'comit'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse("https://codeberg.org/Freeyourgadget/hey%20logs/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse("https://codeberg.org/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse("https://codeberg.org/nbb%20rd/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThat(parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .returns(urlOf("https://codeberg.org"), ForgejoCommitSHALink::getBase)
                .returns("Freeyourgadget", ForgejoCommitSHALink::getOwner)
                .returns("Gadgetbridge", ForgejoCommitSHALink::getRepo)
                .returns("b5d40a0bf012df6c1810eef2c740b8dd7c756843", ForgejoCommitSHALink::getHash)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843");

        assertThat(parse("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .describedAs("case sensitivity")
                .returns(urlOf("https://codeberg.org"), ForgejoCommitSHALink::getBase)
                .returns("FreeyourGADGET", ForgejoCommitSHALink::getOwner)
                .returns("GadgetBRIDGE", ForgejoCommitSHALink::getRepo)
                .returns("b5d40a0bf012df6c1810eef2c740b8dd7c756843", ForgejoCommitSHALink::getHash)
                .hasToString("https://codeberg.org/FreeyourGADGET/GadgetBRIDGE/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843");

        assertThat(parse("https://localhost:8080/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"))
                .returns(urlOf("https://localhost:8080"), ForgejoCommitSHALink::getBase)
                .returns("Freeyourgadget", ForgejoCommitSHALink::getOwner)
                .returns("Gadgetbridge", ForgejoCommitSHALink::getRepo)
                .returns("b5d40a0bf012df6c1810eef2c740b8dd7c756843", ForgejoCommitSHALink::getHash)
                .hasToString("https://localhost:8080/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843");
    }

}