package nbbrd.heylogs.ext.forgejo;

import internal.heylogs.git.Hash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import tests.heylogs.spi.HashConverter;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoCommitRef.of;
import static nbbrd.heylogs.ext.forgejo.ForgejoCommitRef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class ForgejoCommitRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("b5d40a0"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "ForgejoCommitSHARefExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentable(String description, String input, String owner, String repo,
                                  @ConvertWith(HashConverter.class) Hash hash, String output, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(owner, ForgejoCommitRef::getOwner)
                    .returns(repo, ForgejoCommitRef::getRepo)
                    .returns(hash, ForgejoCommitRef::getHash)
                    .hasToString(output);
        } else {
            assertThatIllegalArgumentException()
                    .describedAs(description)
                    .isThrownBy(() -> parse(input))
                    .withMessage(error);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, ForgejoCommitRef.Type.HASH));
        assertThatNullPointerException().isThrownBy(() -> of(commit, (ForgejoCommitRef.Type) null));

        assertThat(of(commit, ForgejoCommitRef.Type.HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, ForgejoCommitRef.Type.OWNER_HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, ForgejoCommitRef.Type.OWNER_REPO_HASH).isCompatibleWith(commit)).isTrue();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testIsCompatibleWith() {
        assertThatNullPointerException().isThrownBy(() -> parse("b5d40a0").isCompatibleWith(null));

        assertThat(parse("b5d40a0").isCompatibleWith(commit)).isTrue();
        assertThat(parse("000007d").isCompatibleWith(commit)).isFalse();

        assertThat(parse("Freeyourgadget@b5d40a0").isCompatibleWith(commit)).isTrue();
        assertThat(parse("Freeyourgadget@000007d").isCompatibleWith(commit)).isFalse();
        assertThat(parse("abcde@b5d40a0").isCompatibleWith(commit)).isFalse();

        assertThat(parse("Freeyourgadget/Gadgetbridge@b5d40a0").isCompatibleWith(commit)).isTrue();
        assertThat(parse("Freeyourgadget/Gadgetbridge@000007d").isCompatibleWith(commit)).isFalse();
        assertThat(parse("abcde/Gadgetbridge@b5d40a0").isCompatibleWith(commit)).isFalse();
        assertThat(parse("Freeyourgadget/abcdefg@b5d40a0").isCompatibleWith(commit)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("b5d40a0").getType()).isEqualTo(ForgejoCommitRef.Type.HASH);
        assertThat(parse("Freeyourgadget@b5d40a0").getType()).isEqualTo(ForgejoCommitRef.Type.OWNER_HASH);
        assertThat(parse("Freeyourgadget/Gadgetbridge@b5d40a0").getType()).isEqualTo(ForgejoCommitRef.Type.OWNER_REPO_HASH);
    }

    private final ForgejoCommitLink commit = ForgejoCommitLink.parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843"));
}