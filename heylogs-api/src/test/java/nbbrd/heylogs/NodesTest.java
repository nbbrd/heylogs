package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import internal.heylogs.ChangelogNodes;
import org.junit.jupiter.api.Test;

import static _test.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;

class NodesTest {

    @Test
    public void testNext() {
        Heading unreleased = ChangelogNodes.getUnreleasedHeading(using("/Main.md")).orElseThrow(RuntimeException::new);

        assertThat(Nodes.next(unreleased, ignore -> false))
                .isEmpty();

        assertThat(Nodes.next(unreleased, ChangelogNodes::isNotVersionHeading))
                .hasSize(4);

        assertThat(Nodes.next(unreleased, ignore -> true))
                .hasSize(75);
    }
}