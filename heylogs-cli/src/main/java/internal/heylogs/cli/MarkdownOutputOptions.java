package internal.heylogs.cli;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.util.ast.Document;
import nbbrd.console.picocli.text.TextOutput;
import nbbrd.console.properties.ConsoleProperties;
import picocli.CommandLine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

@lombok.Getter
@lombok.Setter
public class MarkdownOutputOptions implements TextOutput {

    @CommandLine.Option(
            names = {"-o", "--output"},
            paramLabel = "<file>",
            description = "Output to a file instead of stdout."
    )
    private Path file;

    @Override
    public boolean isGzipped() {
        return false;
    }

    @Override
    public boolean isAppend() {
        return false;
    }

    @Override
    public Charset getEncoding() {
        return UTF_8;
    }

    @Override
    public OutputStream getStdOutStream() {
        return System.out;
    }

    @Override
    public Charset getStdOutEncoding() {
        return ConsoleProperties
                .ofServiceLoader()
                .getStdOutEncoding()
                .orElse(UTF_8);
    }

    public void write(Document document) throws IOException {
        Formatter formatter = Formatter.builder().build();
        try (Writer writer = newCharWriter()) {
            formatter.render(document, writer);
        }
    }
}
