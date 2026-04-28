package nbbrd.heylogs.ext.github;

import nbbrd.heylogs.Check;
import nbbrd.heylogs.Problem;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.*;
import static tests.heylogs.spi.FormatAssert.assertFormatCompliance;

class GitHubActionsFormatTest {

    @Test
    public void testCompliance() {
        assertFormatCompliance(new GitHubActionsFormat());
    }

    @Test
    public void testGetSupportedFormatTypes() {
        Format x = new GitHubActionsFormat();

        assertThat(x.getSupportedFormatTypes())
                .containsExactly(FormatType.PROBLEMS);
    }

    @Test
    public void testFormatProblems() {
        Format x = new GitHubActionsFormat();

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK1))))
                .isEmpty();

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK2))))
                .isEqualTo("::error file=source2,line=5,col=18::boom (rule1)\n");

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK3))))
                .isEqualTo("::error file=source3,line=5,col=18::boom (rule1)\n" +
                        "::error file=source3,line=35,col=2::hello world (rule222)\n");
    }

    @Test
    public void testSeverityMapping() {
        Format x = new GitHubActionsFormat();

        Problem errorProblem = Problem.builder()
                .id("rule1")
                .severity(RuleSeverity.ERROR)
                .issue(RuleIssue.builder().message("error message").line(1).column(1).build())
                .build();

        Problem warnProblem = Problem.builder()
                .id("rule2")
                .severity(RuleSeverity.WARN)
                .issue(RuleIssue.builder().message("warn message").line(2).column(1).build())
                .build();

        Problem offProblem = Problem.builder()
                .id("rule3")
                .severity(RuleSeverity.OFF)
                .issue(RuleIssue.builder().message("notice message").line(3).column(1).build())
                .build();

        Check check = Check.builder()
                .source("CHANGELOG.md")
                .problem(errorProblem)
                .problem(warnProblem)
                .problem(offProblem)
                .build();

        String result = writing(appendable -> x.formatProblems(appendable, singletonList(check)));

        assertThat(result)
                .contains("::error file=CHANGELOG.md,line=1,col=1::error message (rule1)")
                .contains("::warning file=CHANGELOG.md,line=2,col=1::warn message (rule2)")
                .contains("::notice file=CHANGELOG.md,line=3,col=1::notice message (rule3)");
    }

    @Test
    public void testMessageEscaping() {
        Format x = new GitHubActionsFormat();

        Problem problem = Problem.builder()
                .id("rule1")
                .severity(RuleSeverity.ERROR)
                .issue(RuleIssue.builder().message("message with %percent\r\nand newlines").line(1).column(1).build())
                .build();

        Check check = Check.builder()
                .source("CHANGELOG.md")
                .problem(problem)
                .build();

        String result = writing(appendable -> x.formatProblems(appendable, singletonList(check)));

        assertThat(result)
                .isEqualTo("::error file=CHANGELOG.md,line=1,col=1::message with %25percent%0D%0Aand newlines (rule1)\n");
    }

    @Test
    public void testEmptyList() {
        Format x = new GitHubActionsFormat();

        assertThat(writing(appendable -> x.formatProblems(appendable, emptyList())))
                .isEmpty();
    }
}

