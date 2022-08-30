package internal.heylogs.cli;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import nbbrd.console.picocli.text.TextInput;
import nbbrd.console.properties.ConsoleProperties;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

@lombok.Getter
@lombok.Setter
public class MarkdownInputOptions implements TextInput {

    @CommandLine.Option(
            names = {"-i", "--input"},
            paramLabel = "<file>",
            description = "Input from a file instead of stdin."
    )
    private Path file;

    @Override
    public boolean isGzipped() {
        return false;
    }

    @Override
    public Charset getEncoding() {
        return UTF_8;
    }

    @Override
    public InputStream getStdInStream() {
        return System.in;
    }

    @Override
    public Charset getStdInEncoding() {
        return ConsoleProperties
                .ofServiceLoader()
                .getStdInEncoding()
                .orElse(UTF_8);
    }

    public Document read() throws IOException {
        Parser parser = Parser.builder().build();
        try (Reader reader = newCharReader()) {
            return parser.parseReader(reader);
        }
    }
}
