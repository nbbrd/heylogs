package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static nbbrd.heylogs.ext.forgejo.ForgejoCommitSHARef.of;
import static nbbrd.heylogs.ext.forgejo.ForgejoCommitSHARef.parse;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class ForgejoCommitSHARefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("b5d40a0"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "ForgejoCommitSHARefExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String description, String input, String owner, String repo, String hash, String output, String error) {
        if (error == null || error.isEmpty()) {
            assertThat(parse(input))
                    .describedAs(description)
                    .returns(owner, ForgejoCommitSHARef::getOwner)
                    .returns(repo, ForgejoCommitSHARef::getRepo)
                    .returns(hash, ForgejoCommitSHARef::getHash)
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
        assertThatNullPointerException().isThrownBy(() -> of(null, ForgejoCommitSHARef.Type.HASH));
        assertThatNullPointerException().isThrownBy(() -> of(commit, null));

        assertThat(of(commit, ForgejoCommitSHARef.Type.HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, ForgejoCommitSHARef.Type.OWNER_HASH).isCompatibleWith(commit)).isTrue();
        assertThat(of(commit, ForgejoCommitSHARef.Type.OWNER_REPO_HASH).isCompatibleWith(commit)).isTrue();
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
        assertThat(parse("b5d40a0").getType()).isEqualTo(ForgejoCommitSHARef.Type.HASH);
        assertThat(parse("Freeyourgadget@b5d40a0").getType()).isEqualTo(ForgejoCommitSHARef.Type.OWNER_HASH);
        assertThat(parse("Freeyourgadget/Gadgetbridge@b5d40a0").getType()).isEqualTo(ForgejoCommitSHARef.Type.OWNER_REPO_HASH);
    }

    private final ForgejoCommitSHALink commit = ForgejoCommitSHALink.parse("https://codeberg.org/Freeyourgadget/Gadgetbridge/commit/b5d40a0bf012df6c1810eef2c740b8dd7c756843");
}