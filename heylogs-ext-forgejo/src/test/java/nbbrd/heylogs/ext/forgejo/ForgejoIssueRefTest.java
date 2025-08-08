package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoIssueRef.*;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class ForgejoIssueRefTest {

    @Test
    public void testCompliance() {
        assertForgeRefCompliance(parse("#5173"));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("#"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("Gadgetbridge#5173"));

        assertThat(parse("#5173"))
                .returns(null, ForgejoIssueRef::getOwner)
                .returns(null, ForgejoIssueRef::getRepo)
                .returns(5173, ForgejoIssueRef::getIssueNumber)
                .hasToString("#5173");

        assertThat(parse("Freeyourgadget/Gadgetbridge#5173"))
                .returns("Freeyourgadget", ForgejoIssueRef::getOwner)
                .returns("Gadgetbridge", ForgejoIssueRef::getRepo)
                .returns(5173, ForgejoIssueRef::getIssueNumber)
                .hasToString("Freeyourgadget/Gadgetbridge#5173");

        assertThat(parse("FreeyourGADGET/GadgetBRIDGE#5173"))
                .describedAs("case sensitivity")
                .returns("FreeyourGADGET", ForgejoIssueRef::getOwner)
                .returns("GadgetBRIDGE", ForgejoIssueRef::getRepo)
                .returns(5173, ForgejoIssueRef::getIssueNumber)
                .hasToString("FreeyourGADGET/GadgetBRIDGE#5173");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, Type.NUMBER));
        assertThatNullPointerException().isThrownBy(() -> of(issue5173, null));

        assertThat(of(issue5173, Type.NUMBER).isCompatibleWith(issue5173)).isTrue();
        assertThat(of(issue5173, Type.NUMBER).isCompatibleWith(pullRequest5170)).isFalse();
        assertThat(of(pullRequest5170, Type.NUMBER).isCompatibleWith(issue5173)).isFalse();
        assertThat(of(pullRequest5170, Type.NUMBER).isCompatibleWith(pullRequest5170)).isTrue();

        assertThat(of(issue5173, Type.OWNER_REPO_NUMBER).isCompatibleWith(issue5173)).isTrue();
        assertThat(of(issue5173, Type.OWNER_REPO_NUMBER).isCompatibleWith(pullRequest5170)).isFalse();
        assertThat(of(pullRequest5170, Type.OWNER_REPO_NUMBER).isCompatibleWith(issue5173)).isFalse();
        assertThat(of(pullRequest5170, Type.OWNER_REPO_NUMBER).isCompatibleWith(pullRequest5170)).isTrue();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("#5173").isCompatibleWith(issue5173)).isTrue();
        assertThat(parse("Freeyourgadget/Gadgetbridge#5173").isCompatibleWith(issue5173)).isTrue();
        assertThat(parse("jdemetra/jdplus-main#5173").isCompatibleWith(issue5173)).isFalse();
        assertThat(parse("#5173").isCompatibleWith(pullRequest5170)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("#5173").getType()).isEqualTo(Type.NUMBER);
        assertThat(parse("Freeyourgadget/Gadgetbridge#5173").getType()).isEqualTo(Type.OWNER_REPO_NUMBER);
    }

    private final ForgejoIssueLink issue5173 = ForgejoIssueLink.parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/5173"));
    private final ForgejoIssueLink pullRequest5170 = ForgejoIssueLink.parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/5170"));
}