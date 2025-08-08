package internal.heylogs.spi;

import internal.heylogs.git.ThreeDotDiff;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ThreeDotDiffTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException().isThrownBy(() -> ThreeDotDiff.parse("v0.7.2..HEAD"));
        assertThatIllegalArgumentException().isThrownBy(() -> ThreeDotDiff.parse("...HEAD"));
        assertThatIllegalArgumentException().isThrownBy(() -> ThreeDotDiff.parse("v0.7.2..."));

        assertThat(ThreeDotDiff.parse("v0.7.2...HEAD"))
                .returns("v0.7.2", ThreeDotDiff::getFrom)
                .returns("HEAD", ThreeDotDiff::getTo)
                .hasToString("v0.7.2...HEAD");

        assertThat(ThreeDotDiff.parse("3e47abcfffc7388737ea671e3ee806968fc18417...ac9318b20caf0a7eb3927edac95d3344f7df10a7"))
                .returns("3e47abcfffc7388737ea671e3ee806968fc18417", ThreeDotDiff::getFrom)
                .returns("ac9318b20caf0a7eb3927edac95d3344f7df10a7", ThreeDotDiff::getTo)
                .hasToString("3e47abcfffc7388737ea671e3ee806968fc18417...ac9318b20caf0a7eb3927edac95d3344f7df10a7");
    }

    @Test
    public void testDerive() {
        assertThat(ThreeDotDiff.parse("v1.0.0...v1.1.0").derive("v2.0.0"))
                .returns("v1.1.0", ThreeDotDiff::getFrom)
                .returns("v2.0.0", ThreeDotDiff::getTo)
                .hasToString("v1.1.0...v2.0.0");

        assertThat(ThreeDotDiff.parse("v1.0.0...HEAD").derive("v2.0.0"))
                .returns("v1.0.0", ThreeDotDiff::getFrom)
                .returns("v2.0.0", ThreeDotDiff::getTo)
                .hasToString("v1.0.0...v2.0.0");

        assertThat(ThreeDotDiff.parse("HEAD...HEAD").derive("v2.0.0"))
                .returns("v2.0.0", ThreeDotDiff::getFrom)
                .returns("v2.0.0", ThreeDotDiff::getTo)
                .hasToString("v2.0.0...v2.0.0");
    }
}