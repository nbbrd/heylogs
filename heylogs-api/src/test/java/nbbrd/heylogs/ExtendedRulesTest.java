package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.ExtendedRules;
import nbbrd.heylogs.Sample;
import nbbrd.heylogs.Nodes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedRulesTest {

    @Test
    public void test() throws IOException {
        Node sample = Sample.get();
        for (ExtendedRules rule : ExtendedRules.values()) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }
}
