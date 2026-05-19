package nbbrd.heylogs.ext.github;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Problem;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatSupport;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@DirectImpl
@ServiceProvider
public final class GitHubActionsFormat implements Format {

    public static final String ID = "github-actions";

    @lombok.experimental.Delegate
    private final FormatSupport delegate = FormatSupport
            .builder()
            .id(ID)
            .name("GitHub Actions workflow commands")
            .moduleId("github")
            .problems(this::formatProblemsAsWorkflowCommands)
            .filter(file -> false)
            .build();

    private void formatProblemsAsWorkflowCommands(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        for (Check check : list) {
            for (Problem problem : check.getProblems()) {
                appendable.append(formatProblem(check.getSource(), problem));
                appendable.append('\n');
            }
        }
    }

    private static @NonNull String formatProblem(@NonNull String filePath, @NonNull Problem problem) {
        String command = toWorkflowCommand(problem.getSeverity());
        String message = problem.getIssue().getMessage() + " (" + problem.getId() + ")";

        return String.format(Locale.ROOT, "%s file=%s,line=%d,col=%d::%s",
                command,
                filePath,
                problem.getIssue().getLine(),
                problem.getIssue().getColumn(),
                escapeMessage(message));
    }

    private static @NonNull String toWorkflowCommand(@NonNull RuleSeverity severity) {
        switch (severity) {
            case ERROR:
                return "::error";
            case WARN:
                return "::warning";
            case OFF:
                return "::notice";
            default:
                throw new IllegalArgumentException("Unknown severity: " + severity);
        }
    }

    private static @NonNull String escapeMessage(@NonNull String message) {
        return message
                .replace("%", "%25")
                .replace("\r", "%0D")
                .replace("\n", "%0A");
    }
}



