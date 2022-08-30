package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import org.jetbrains.annotations.NotNull;

public enum ExtendedRules implements Rule<Node> {

    LIMIT_HEADING_DEPTH {
        @Override
        public String validate(Node node) {
            return node instanceof Heading ? checkHeading((Heading) node) : null;
        }

        private String checkHeading(@NotNull Heading heading) {
            if (heading.getLevel() > 3) {
                return Rule.invalidNode(heading, "not expecting this level, found '" + heading.getText() + "'");
            }
            return null;
        }
    },
    HTTPS {
        @Override
        public String validate(Node node) {
            return null;
        }
    },
    GITHUB_ISSUE_REF {
        @Override
        public String validate(Node node) {
            return null;
        }
    }
}
