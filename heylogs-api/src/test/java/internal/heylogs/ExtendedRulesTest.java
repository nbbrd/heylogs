package internal.heylogs;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ExtendedRules;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Sample;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static internal.heylogs.ExtendedRules.GITHUB_ISSUE_REF;
import static internal.heylogs.ExtendedRules.HTTPS;
import static nbbrd.heylogs.Nodes.of;
import static nbbrd.heylogs.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class ExtendedRulesTest {

    @Test
    public void test() {
        Node sample = Sample.using("Main.md");
        for (ExtendedRules rule : ExtendedRules.values()) {
            Assertions.assertThat(Nodes.of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateHttps() {
        assertThat(of(LinkNodeBase.class).descendants(using("Main.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(LinkNodeBase.class).descendants(using("NonHttps.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.of(HTTPS, "Expecting HTTPS protocol", 1, 1), atIndex(0))
                .contains(Failure.of(HTTPS, "Expecting HTTPS protocol", 2, 7), atIndex(1))
                .hasSize(2); // FIXME: should be 3
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(of(Link.class).descendants(using("Main.md")))
                .map(ExtendedRules::validateGitHubIssueRef)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("InvalidGitHubIssueRef.md")))
                .map(ExtendedRules::validateGitHubIssueRef)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.of(GITHUB_ISSUE_REF, "Expecting GitHub issue ref 172, found 173", 2, 1), atIndex(0))
                .hasSize(1);
    }
}
