package nbbrd.heylogs;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.GuidingPrinciples;
import nbbrd.heylogs.Sample;
import nbbrd.heylogs.Nodes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.System.lineSeparator;
import static nbbrd.heylogs.Sample.HEADING_UNRELEASED;
import static nbbrd.heylogs.Sample.HEADING_V1_1_0;
import static org.assertj.core.api.Assertions.assertThat;

public class GuidingPrinciplesTest {

    @Test
    public void testSample() throws IOException {
        Node sample = Sample.get();
        for (GuidingPrinciples rule : GuidingPrinciples.values()) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateLatestVersionFirst() {
        Parser parser = Parser.builder().build();
        Function<String, String> x = md -> GuidingPrinciples.validateLatestVersionFirst(parser.parse(md));

        assertThat(x.apply(""))
                .isNull();

        assertThat(x.apply(HEADING_UNRELEASED.concat(lineSeparator()).concat(HEADING_V1_1_0)))
                .isNull();

        assertThat(x.apply(HEADING_V1_1_0.concat(lineSeparator()).concat(HEADING_UNRELEASED)))
                .contains("Invalid Heading node at line 1", "not sorted");
    }

}
