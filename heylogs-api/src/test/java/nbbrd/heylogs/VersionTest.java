package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Sample;
import nbbrd.heylogs.Version;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class VersionTest {

    @Test
    public void test() throws IOException {
        LocalDate d20190215 = LocalDate.parse("2019-02-15");

        assertThat(Version.parse(asHeading("## [Unreleased]")))
                .isEqualTo(new Version("Unreleased", true, LocalDate.MAX));

        assertThat(Version.parse(asHeading("## Unreleased")))
                .isEqualTo(new Version("Unreleased", false, LocalDate.MAX));

        assertThat(Version.parse(asHeading("## [1.1.0] - 2019-02-15")))
                .isEqualTo(new Version("1.1.0", true, d20190215));

//        assertThat(Version.parse(asHeading("## 1.1.0 - 2019-02-15")))
//                .isEqualTo(new Version("1.1.0", false, d20190215));

//        assertThatIllegalArgumentException()
//                .isThrownBy(()->Version.parse(asHeading("## [1.1.0](https://localhost) - 2019-02-15")));

//        assertThatIllegalArgumentException()
//                .isThrownBy(() -> Version.parse(asHeading("## [1.1.0] - ")))
//                .withMessageContaining("Invalid date");
//
//        assertThatIllegalArgumentException()
//                .isThrownBy(() -> Version.parse(asHeading("## [1.1.0] - 2019-02")))
//                .withMessageContaining("Invalid date");
//
//        assertThatIllegalArgumentException()
//                .isThrownBy(() -> Version.parse(asHeading("## - 2019-02-15")))
//                .withMessageContaining("Missing version");

        assertThat(Nodes.of(Heading.class).descendants(Sample.get()).filter(Version::isVersionLevel).map(Version::parse))
                .hasSize(14)
                .contains(new Version("Unreleased", true, LocalDate.MAX), atIndex(0))
                .contains(new Version("1.1.0", true, d20190215), atIndex(1));

    }

    final Parser parser = Parser.builder().build();

    private Heading asHeading(String text) {
        return (Heading) parser.parse(text).getChildOfType(Heading.class);
    }
}
