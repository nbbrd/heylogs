package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import static internal.heylogs.HeylogsParameters.DEFAULT_FORMAT_ID;

@lombok.Builder(toBuilder = true)
public final class FormatSupport implements Format {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    @lombok.Singular
    private final @NonNull Set<FormatType> types;

    private final @Nullable ItemsFormatter<Check> problems;

    private final @Nullable ItemsFormatter<Scan> status;

    private final @Nullable ItemsFormatter<Resource> resources;

    private final @Nullable ContentFormatter content;

    private final @Nullable ContentParser contentParser;

    private final @NonNull DirectoryStream.Filter<? super Path> filter;

    @Override
    public @NonNull String getFormatId() {
        return id;
    }

    @Override
    public @NonNull String getFormatName() {
        return name;
    }

    @Override
    public @NonNull String getFormatModuleId() {
        return moduleId;
    }

    @Override
    public @NonNull Set<FormatType> getSupportedFormatTypes() {
        return types;
    }

    @Override
    public void formatProblems(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        if (problems == null)
            throw new UnsupportedOperationException("formatProblems not supported by format '" + getFormatId() + "'");
        problems.formatItems(appendable, list);
    }

    @Override
    public void formatStatus(@NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        if (status == null)
            throw new UnsupportedOperationException("formatStatus not supported by format '" + getFormatId() + "'");
        status.formatItems(appendable, list);
    }

    @Override
    public void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        if (resources == null)
            throw new UnsupportedOperationException("formatResources not supported by format '" + getFormatId() + "'");
        resources.formatItems(appendable, list);
    }

    @Override
    public void formatContent(@NonNull Appendable appendable, @NonNull ChangelogContent changelogContent) throws IOException {
        if (content == null)
            throw new UnsupportedOperationException("formatContent not supported by format '" + getFormatId() + "'");
        content.formatContent(appendable, changelogContent);
    }

    @Override
    public @NonNull ChangelogContent parseContent(@NonNull Reader reader) throws IOException {
        if (contentParser == null)
            throw new UnsupportedOperationException("parseContent not supported by format '" + getFormatId() + "'");
        return contentParser.parseContent(reader);
    }

    @Override
    public @NonNull DirectoryStream.Filter<? super Path> getFormatFileFilter() {
        return filter;
    }

    @FunctionalInterface
    public interface ItemsFormatter<T> {

        void formatItems(@NonNull Appendable appendable, @NonNull List<T> list) throws IOException;
    }

    @FunctionalInterface
    public interface ContentFormatter {

        void formatContent(@NonNull Appendable appendable, @NonNull ChangelogContent content) throws IOException;
    }

    @FunctionalInterface
    public interface ContentParser {

        @NonNull
        ChangelogContent parseContent(@NonNull Reader reader) throws IOException;
    }

    public static final class Builder {

        public @NonNull Builder id(@NonNull String id) {
            this.id = checkFormatId(id);
            return this;
        }

        public @NonNull Builder problems(@NonNull ItemsFormatter<Check> problems) {
            this.problems = problems;
            return type(FormatType.PROBLEMS);
        }

        public @NonNull Builder status(@NonNull ItemsFormatter<Scan> status) {
            this.status = status;
            return type(FormatType.STATUS);
        }

        public @NonNull Builder resources(@NonNull ItemsFormatter<Resource> resources) {
            this.resources = resources;
            return type(FormatType.RESOURCES);
        }

        public @NonNull Builder content(@NonNull ContentFormatter content) {
            this.content = content;
            return type(FormatType.CONTENT);
        }

        public @NonNull Builder contentParser(@NonNull ContentParser contentParser) {
            this.contentParser = contentParser;
            return this;
        }

        public @NonNull Builder filterByExtension(@NonNull String extension) {
            return filter(getFormatFileFilterByExtension(extension));
        }
    }

    @MightBeGenerated
    public static String checkFormatId(@NonNull String id) {
        if (!FormatLoader.ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid format id '" + id + "', should follow pattern " + FormatLoader.ID_PATTERN.pattern());
        }
        return id;
    }

    public static @NonNull DirectoryStream.Filter<? super Path> getFormatFileFilterByExtension(@NonNull String extension) {
        return file -> (!Files.exists(file) || Files.isRegularFile(file)) && FormatSupport.hasExtension(file, extension);
    }

    public static boolean hasExtension(@NonNull Path file, @NonNull String extension) {
        return file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(extension);
    }

    public static @NonNull String resolveFormatId(@Nullable FormatConfig format, @NonNull Heylogs heylogs, @NonNull Predicate<? super Path> stdio, @NonNull Path file, @NonNull FormatType type) {
        String formatId = format != null ? format.getId() : null;
        if (isSpecified(formatId)) return formatId;
        String fallback = heylogs.getFirstFormatIdByType(type).orElse(DEFAULT_FORMAT_ID);
        return !stdio.test(file) ? heylogs.getFormatIdByFile(file).orElse(fallback) : fallback;
    }

    private static boolean isSpecified(String formatId) {
        return formatId != null && !formatId.isEmpty();
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Format> onFormatType(@NonNull FormatType type) {
        return format -> format.getSupportedFormatTypes().contains(type);
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Format> onFormatId(@NonNull String id) {
        return format -> id.equals(Heylogs.FIRST_FORMAT_AVAILABLE) || format.getFormatId().equals(id);
    }

    @StaticFactoryMethod(Predicate.class)
    public static @NonNull Predicate<Format> onFormatFileFilter(@NonNull Path file) {
        return format -> {
            try {
                return format.getFormatFileFilter().accept(file);
            } catch (IOException e) {
                return false;
            }
        };
    }
}
