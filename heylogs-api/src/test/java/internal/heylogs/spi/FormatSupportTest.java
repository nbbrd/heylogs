package internal.heylogs.spi;

import internal.heylogs.base.StylishFormat;
import nbbrd.heylogs.FormatConfig;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.spi.FormatSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.function.Predicate;

import static nbbrd.heylogs.spi.FormatSupport.resolveFormatId;
import static org.assertj.core.api.Assertions.assertThat;

class FormatSupportTest {

    @Test
    void testResolveFormatId(@TempDir Path tmp) {
        Heylogs heylogs = Heylogs
                .builder()
                .format(new StylishFormat())
                .format(FormatSupport
                        .builder()
                        .id("stuff")
                        .name("stuff")
                        .moduleId("stuff")
                        .filterByExtension(".stuff")
                        .build())
                .build();

        Path unknown = tmp.resolve("test.unknown");
        Path stuff = tmp.resolve("test.stuff");

        Predicate<Path> stdio = ignore -> true;
        Predicate<Path> file = ignore -> false;

        assertThat(resolveFormatId(null, heylogs, file, unknown)).isEqualTo("stylish");
        assertThat(resolveFormatId(null, heylogs, stdio, unknown)).isEqualTo("stylish");
        assertThat(resolveFormatId(null, heylogs, file, stuff)).isEqualTo("stuff");
        assertThat(resolveFormatId(null, heylogs, stdio, stuff)).isEqualTo("stylish");

//        assertThat(resolveFormatId(FormatConfig.parse(""), heylogs, file, unknown)).isEqualTo("stylish");
//        assertThat(resolveFormatId(FormatConfig.parse(""), heylogs, stdio, unknown)).isEqualTo("stylish");
//        assertThat(resolveFormatId(FormatConfig.parse(""), heylogs, file, stuff)).isEqualTo("stuff");
//        assertThat(resolveFormatId(FormatConfig.parse(""), heylogs, stdio, stuff)).isEqualTo("stylish");

        assertThat(resolveFormatId(FormatConfig.parse("other"), heylogs, file, unknown)).isEqualTo("other");
        assertThat(resolveFormatId(FormatConfig.parse("other"), heylogs, stdio, unknown)).isEqualTo("other");
        assertThat(resolveFormatId(FormatConfig.parse("other"), heylogs, file, stuff)).isEqualTo("other");
        assertThat(resolveFormatId(FormatConfig.parse("other"), heylogs, stdio, stuff)).isEqualTo("other");
    }
}