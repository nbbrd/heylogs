package internal.heylogs;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Status;
import nbbrd.heylogs.spi.Format;
import nbbrd.io.text.Formatter;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Locale.ROOT;

// https://eslint.org/docs/latest/user-guide/formatters/#stylish
@ServiceProvider
public final class StylishFormat implements Format {

    public static final String ID = "stylish";

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public void formatFailures(@NonNull Appendable appendable, @NonNull String source, @NonNull List<Failure> failures) throws IOException {
        StylishWriter
                .<Failure>builder()
                .column(getPositionFormatter(failures))
                .column(Formatter.onConstant("error"))
                .column(Formatter.of(Failure::getMessage))
                .column(Formatter.of(Failure::getRuleId))
                .build()
                .write(appendable, source, failures, getFailuresSummary(failures));
    }

    @MightBePromoted
    private static Formatter<Failure> getPositionFormatter(List<Failure> failures) {
        int l = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getLine())).max().orElse(0);
        int c = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getColumn())).max().orElse(0);
        String format = "%" + l + "d:%-" + c + "d";
        return Formatter.of(failure -> String.format(ROOT, format, failure.getLine(), failure.getColumn()));
    }

    @MightBePromoted
    private static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }

    private String getFailuresSummary(List<Failure> list) {
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
    public void formatStatus(@NonNull Appendable appendable, @NonNull String source, @NonNull Status status) throws IOException {
        StylishWriter
                .<String>builder()
                .column(Formatter.onString())
                .build()
                .write(appendable, source, getStatusBody(status), null);
    }

    private List<String> getStatusBody(Status status) {
        if (status.getReleaseCount() == 0) {
            return asList(
                    "No release found",
                    status.isHasUnreleasedSection() ? "Has an unreleased version" : "Has no unreleased version"
            );
        } else {
            return asList(
                    String.format(ROOT, "Found %d releases", status.getReleaseCount()),
                    String.format(ROOT, "Ranging from %s to %s", status.getTimeRange().getFrom(), status.getTimeRange().getTo()),
                    status.isCompatibleWithSemver()
                            ? "Compatible with Semantic Versioning" + status.getSemverDetails()
                            : "Not compatible with Semantic Versioning",
                    status.isHasUnreleasedSection() ? "Has an unreleased version" : "Has no unreleased version"
            );
        }
    }

    @Override
    public void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> resources) throws IOException {
        StylishWriter
                .<Resource>builder()
                .column(Formatter.of(Resource::getType))
                .column(Formatter.of(Resource::getId))
                .build()
                .write(appendable, "Resources", resources, getResourcesSummary(resources));
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
