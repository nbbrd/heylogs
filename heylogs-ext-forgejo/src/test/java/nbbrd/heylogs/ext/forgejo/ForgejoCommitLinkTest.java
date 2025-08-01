package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.net.URL;

import static nbbrd.heylogs.ext.forgejo.ForgejoCommitLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class ForgejoCommitLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "ForgejoCommitSHALinkExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, URL base, String owner, String repo, String hash, String output, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(base, ForgejoCommitLink::getBase)
                    .returns(owner, ForgejoCommitLink::getOwner)
                    .returns(repo, ForgejoCommitLink::getRepo)
                    .returns(hash, ForgejoCommitLink::getHash)
                    .hasToString(output);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
                    .withMessage(error);
        }
    }
}