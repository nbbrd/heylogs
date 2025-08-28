package nbbrd.heylogs.ext.forgejo;

import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoRequestRef.*;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.spi.ForgeRefAssert.assertForgeRefCompliance;

class ForgejoRequestRefTest {

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
                .returns(null, ForgejoRequestRef::getOwner)
                .returns(null, ForgejoRequestRef::getRepo)
                .returns(5173, ForgejoRequestRef::getRequestNumber)
                .hasToString("#5173");

        assertThat(parse("Freeyourgadget/Gadgetbridge#5173"))
                .returns("Freeyourgadget", ForgejoRequestRef::getOwner)
                .returns("Gadgetbridge", ForgejoRequestRef::getRepo)
                .returns(5173, ForgejoRequestRef::getRequestNumber)
                .hasToString("Freeyourgadget/Gadgetbridge#5173");

        assertThat(parse("FreeyourGADGET/GadgetBRIDGE#5173"))
                .describedAs("case sensitivity")
                .returns("FreeyourGADGET", ForgejoRequestRef::getOwner)
                .returns("GadgetBRIDGE", ForgejoRequestRef::getRepo)
                .returns(5173, ForgejoRequestRef::getRequestNumber)
                .hasToString("FreeyourGADGET/GadgetBRIDGE#5173");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> of(null, Type.NUMBER));
        assertThatNullPointerException().isThrownBy(() -> of(issue5173, (Type) null));

        assertThat(of(issue5173, Type.NUMBER).isCompatibleWith(issue5173)).isTrue();
        assertThat(of(issue5173, Type.NUMBER).isCompatibleWith(issue51730)).isFalse();

        assertThat(of(issue5173, Type.OWNER_REPO_NUMBER).isCompatibleWith(issue5173)).isTrue();
        assertThat(of(issue5173, Type.OWNER_REPO_NUMBER).isCompatibleWith(issue51730)).isFalse();
    }

    @Test
    public void testIsCompatibleWith() {
        assertThat(parse("#5173").isCompatibleWith(issue5173)).isTrue();
        assertThat(parse("Freeyourgadget/Gadgetbridge#5173").isCompatibleWith(issue5173)).isTrue();
        assertThat(parse("jdemetra/jdplus-main#5173").isCompatibleWith(issue5173)).isFalse();
        assertThat(parse("#5173").isCompatibleWith(issue51730)).isFalse();
    }

    @Test
    public void testGetType() {
        assertThat(parse("#5173").getType()).isEqualTo(Type.NUMBER);
        assertThat(parse("Freeyourgadget/Gadgetbridge#5173").getType()).isEqualTo(Type.OWNER_REPO_NUMBER);
    }

    private final ForgejoRequestLink issue5173 = ForgejoRequestLink.parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/5173"));
    private final ForgejoRequestLink issue51730 = ForgejoRequestLink.parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/pulls/51700"));
}