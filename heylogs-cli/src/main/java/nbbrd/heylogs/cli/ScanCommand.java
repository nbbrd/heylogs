package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.cli.MarkdownInputOptions;
import nbbrd.heylogs.Scan;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "scan")
public final class ScanCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @Override
    public Void call() throws Exception {
        Node document = input.read();
        Scan report = Scan.of(document);
        print(report);
        return null;
    }

    private void print(Scan report) {
        System.out.println(input.getFile());
        if (report.getReleaseCount() == 0) {
            System.out.println("  No release found");
        } else {
            System.out.printf("  Found %d releases%n", report.getReleaseCount());
            System.out.printf("  Ranging from %s to %s%n", report.getTimeRange().getFrom(), report.getTimeRange().getTo());

            if (report.isCompatibleWithSemver())
                System.out.println("  Compatible with Semantic Versioning" + report.getSemverDetails());
            else
                System.out.println("  Not compatible with Semantic Versioning");
        }
        System.out.println(report.isHasUnreleasedSection() ? "  Has an unreleased version" : "  Has no unreleased version");
    }
}
