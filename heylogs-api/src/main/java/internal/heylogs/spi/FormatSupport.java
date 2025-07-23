package internal.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Heylogs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Predicate;

import static internal.heylogs.HeylogsParameters.DEFAULT_FORMAT_ID;

public final class FormatSupport {

    private FormatSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull DirectoryStream.Filter<? super Path> getFormatFileFilterByExtension(@NonNull String extension) {
        return file -> (!Files.exists(file) || Files.isRegularFile(file)) && FormatSupport.hasExtension(file, extension);
    }

    public static boolean hasExtension(@NonNull Path file, @NonNull String extension) {
        return file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(extension);
    }

    public static @NonNull String resolveFormatId(@Nullable String formatId, @NonNull Heylogs heylogs, @NonNull Predicate<? super Path> stdio, @NonNull Path file) {
        return isSpecified(formatId) ? formatId : !stdio.test(file) ? heylogs.getFormatIdByFile(file).orElse(DEFAULT_FORMAT_ID) : DEFAULT_FORMAT_ID;
    }

    private static boolean isSpecified(String formatId) {
        return formatId != null && !formatId.isEmpty();
    }

}
