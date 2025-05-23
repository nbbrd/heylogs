package internal.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Scan;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static internal.heylogs.FormatSupport.resolveFormatId;
import static org.assertj.core.api.Assertions.assertThat;

class FormatSupportTest {

    @Test
    void testResolveFormatId(@TempDir Path tmp) {
        Heylogs heylogs = Heylogs
                .builder()
                .format(new StylishFormat())
                .format(StuffFormat.INSTANCE)
                .build();

        Path unknown = tmp.resolve("test.unknown");
        Path stuff = tmp.resolve("test.stuff");

        Predicate<Path> stdio = ignore -> true;
        Predicate<Path> file = ignore -> false;

        assertThat(resolveFormatId(null, heylogs, file, unknown)).isEqualTo("stylish");
        assertThat(resolveFormatId(null, heylogs, stdio, unknown)).isEqualTo("stylish");
        assertThat(resolveFormatId(null, heylogs, file, stuff)).isEqualTo("stuff");
        assertThat(resolveFormatId(null, heylogs, stdio, stuff)).isEqualTo("stylish");

        assertThat(resolveFormatId("", heylogs, file, unknown)).isEqualTo("stylish");
        assertThat(resolveFormatId("", heylogs, stdio, unknown)).isEqualTo("stylish");
        assertThat(resolveFormatId("", heylogs, file, stuff)).isEqualTo("stuff");
        assertThat(resolveFormatId("", heylogs, stdio, stuff)).isEqualTo("stylish");

        assertThat(resolveFormatId("other", heylogs, file, unknown)).isEqualTo("other");
        assertThat(resolveFormatId("other", heylogs, stdio, unknown)).isEqualTo("other");
        assertThat(resolveFormatId("other", heylogs, file, stuff)).isEqualTo("other");
        assertThat(resolveFormatId("other", heylogs, stdio, stuff)).isEqualTo("other");
    }

    private enum StuffFormat implements Format {

        INSTANCE;

        @Override
        public @NonNull String getFormatId() {
            return "stuff";
        }

        @Override
        public @NonNull String getFormatName() {
            return "stuff";
        }

        @Override
        public @NonNull String getFormatCategory() {
            return "";
        }

        @Override
        public @NonNull Set<FormatType> getSupportedFormatTypes() {
            return Collections.emptySet();
        }

        @Override
        public void formatProblems(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        }

        @Override
        public void formatStatus(@NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        }

        @Override
        public void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        }

        @Override
        public @NonNull DirectoryStream.Filter<? super Path> getFormatFileFilter() {
            return FormatSupport.getFormatFileFilterByExtension(".stuff");
        }
    }
}