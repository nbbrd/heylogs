package internal.heylogs;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import nbbrd.io.text.Formatter;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Locale.ROOT;

// https://eslint.org/docs/latest/user-guide/formatters/#stylish
@DirectImpl
@ServiceProvider
public final class StylishFormat implements Format {

    public static final String ID = "stylish";

    @Override
    public @NonNull String getFormatId() {
        return ID;
    }

    @Override
    public @NonNull String getFormatName() {
        return "Human-readable output";
    }

    @Override
    public @NonNull String getFormatCategory() {
        return "interaction";
    }

    @Override
    public @NonNull Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.allOf(FormatType.class);
    }

    @Override
    public void formatProblems(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        StylishWriter<Problem> writer = StylishWriter
                .<Problem>builder()
                .column(getPositionFormatter(list))
                .column(getRuleSeverityFormatter())
                .column(Formatter.of(problem -> problem.getIssue().getMessage()))
                .column(Formatter.of(Problem::getId))
                .build();
        for (Check item : list) {
            writer.write(appendable, item.getSource(), item.getProblems(), getProblemsSummary(item.getProblems()));
        }
    }

    @MightBePromoted
    private static Formatter<Problem> getPositionFormatter(List<Check> problems) {
        int l = problems.stream().flatMap(o -> o.getProblems().stream()).mapToInt(problem -> getNumberOfDigits(problem.getIssue().getLine())).max().orElse(0);
        int c = problems.stream().flatMap(o -> o.getProblems().stream()).mapToInt(problem -> getNumberOfDigits(problem.getIssue().getColumn())).max().orElse(0);
        String format = "%" + l + "d:%-" + c + "d";
        return Formatter.of(problem -> String.format(ROOT, format, problem.getIssue().getLine(), problem.getIssue().getColumn()));
    }

    private static Formatter<Problem> getRuleSeverityFormatter() {
        return Formatter.of(problem -> {
            switch (problem.getSeverity()) {
                case OFF:
                    return "off";
                case WARN:
                    return "warning";
                case ERROR:
                    return "error";
                default:
                    throw new RuntimeException();
            }
        });
    }

    @MightBePromoted
    private static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }

    private String getProblemsSummary(List<Problem> list) {
        switch (list.size()) {
            case 0:
                return "No problem";
            case 1:
                return "1 problem";
            default:
                return list.size() + " problems";
        }
    }

    @Override
    public void formatStatus(@NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        StylishWriter<String> writer = StylishWriter
                .<String>builder()
                .column(Formatter.onString())
                .build();
        for (Scan item : list) {
            writer.write(appendable, item.getSource(), getStatusBody(item.getSummary()), null);
        }
    }

    private List<String> getStatusBody(Summary summary) {
        if (summary.getReleaseCount() == 0) {
            return asList(
                    "No release found",
                    summary.isHasUnreleasedSection() ? "Has an unreleased version" : "Has no unreleased version"
            );
        } else {
            return asList(
                    String.format(ROOT, "Found %d releases", summary.getReleaseCount()),
                    String.format(ROOT, "Ranging from %s to %s", summary.getTimeRange().getFrom(), summary.getTimeRange().getTo()),
                    summary.getCompatibilities().isEmpty()
                            ? "Not compatible with known versioning"
                            : "Compatible with " + String.join(", ", summary.getCompatibilities()),
                    summary.isHasUnreleasedSection() ? "Has an unreleased version" : "Has no unreleased version"
            );
        }
    }

    @Override
    public void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        StylishWriter
                .<Resource>builder()
                .column(Formatter.of(Resource::getType))
                .column(Formatter.of(Resource::getCategory))
                .column(Formatter.of(Resource::getId))
                .column(Formatter.of(Resource::getName))
                .build()
                .write(appendable, "Resources", list, getResourcesSummary(list));
    }

    private String getResourcesSummary(List<Resource> list) {
        switch (list.size()) {
            case 0:
                return "No resource found";
            case 1:
                return "1 resource found";
            default:
                return list.size() + " resources found";
        }
    }

    @lombok.Value
    @lombok.Builder
    private static class StylishWriter<T> {

        @lombok.Builder.Default
        String delimiter = "  ";

        @lombok.Builder.Default
        String separator = lineSeparator();

        @lombok.Singular
        List<Formatter<T>> columns;

        public void write(Appendable appendable, CharSequence header, List<T> body, CharSequence footer) throws IOException {
            writeHeader(appendable, header);
            writeBody(appendable, body);
            if (footer != null)
                writeFooter(appendable, footer);
        }

        private void writeHeader(Appendable appendable, CharSequence header) throws IOException {
            appendable.append(header);
            appendable.append(separator);
        }

        private void writeBody(Appendable appendable, List<T> body) throws IOException {
            List<CharSequence[]> rows = new ArrayList<>();
            for (T value : body) {
                CharSequence[] row = new CharSequence[columns.size()];
                for (int i = 0; i < row.length; i++) {
                    row[i] = columns.get(i).format(value);
                }
                rows.add(row);
            }
            int[] sizes = new int[columns.size()];
            for (CharSequence[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    sizes[i] = Math.max(sizes[i], row[i].length());
                }
            }
            String[] patterns = new String[columns.size()];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = "%-" + sizes[i] + "s";
            }
            for (CharSequence[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    appendable.append(delimiter).append(String.format(ROOT, patterns[i], row[i]));
                }
                appendable.append(separator);
            }
        }

        private void writeFooter(Appendable appendable, CharSequence footer) throws IOException {
            appendable.append(delimiter).append(separator);
            appendable.append(delimiter).append(footer);
            appendable.append(separator);
        }
    }
}
