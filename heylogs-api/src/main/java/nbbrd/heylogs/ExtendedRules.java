package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

public enum ExtendedRules implements Rule {

    LIMIT_HEADING_DEPTH {
        @Override
        public Failure validate(Node node) {
            return node instanceof Heading ? validateLimitHeadingDepth((Heading) node) : null;
        }
    },
    HTTPS {
        @Override
        public Failure validate(Node node) {
            return node instanceof LinkNodeBase ? validateHttps((LinkNodeBase) node) : null;
        }
    },
    GITHUB_ISSUE_REF {
        @Override
        public Failure validate(Node node) {
            return node instanceof Link ? validateGitHubIssueRef((Link) node) : null;
        }
    };

    @VisibleForTesting
    static Failure validateLimitHeadingDepth(@NotNull Heading heading) {
        return heading.getLevel() > 3
                ? Failure.of(LIMIT_HEADING_DEPTH, "Not expecting level " + heading.getLevel() + "", heading)
                : null;
    }

    @VisibleForTesting
    static Failure validateHttps(LinkNodeBase link) {
        try {
            if (new URL(link.getUrl().toString()).getProtocol().equals("http")) {
                return Failure.of(HTTPS, "Expecting HTTPS protocol", link);
            }
        } catch (MalformedURLException e) {
        }
        return null;
    }

    @VisibleForTesting
    static Failure validateGitHubIssueRef(Link link) {
        int expected = getGitHubIssueRefFromURL(link);
        int found = getGitHubIssueRefFromText(link);
        return expected != -1 && found != -1 && expected != found
                ? Failure.of(GITHUB_ISSUE_REF, "Expecting GitHub issue ref " + expected + ", found " + found, link)
                : null;
    }

    private static int getGitHubIssueRefFromURL(Link link) {
        try {
            URL url = new URL(link.getUrl().toString());
            if (url.getHost().equals("github.com")) {
                int index = url.getPath().indexOf("/issues/");
                if (index != -1) {
                    return Integer.parseInt(url.getPath().substring(index + 8));
                }
            }
        } catch (MalformedURLException | NumberFormatException ex) {
        }
        return -1;
    }

    private static int getGitHubIssueRefFromText(Link link) {
        try {
            String text = link.getText().toString();
            if (text.startsWith("#")) {
                return Integer.parseInt(text.substring(1));
            }
        } catch (NumberFormatException ex) {
        }
        return -1;
    }
}
