package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.VersionHeading;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.using;

class NodesTest {

    @Test
    public void testNextWhile() {
        Heading unreleased = ChangelogHeading.root(using("/Main.md"))
                .orElseThrow(RuntimeException::new)
                .getVersions()
                .filter(version -> version.getSection().isUnreleased())
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getHeading();

        assertThat(Nodes.nextWhile(unreleased, ignore -> false))
                .isEmpty();

        assertThat(Nodes.nextWhile(unreleased, node -> !VersionHeading.isParsable(node)))
                .hasSize(4);

        assertThat(Nodes.nextWhile(unreleased, ignore -> true))
                .hasSize(75);
    }
}