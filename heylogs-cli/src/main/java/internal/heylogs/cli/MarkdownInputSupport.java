package internal.heylogs.cli;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.console.picocli.CommandSupporter;
import nbbrd.console.picocli.text.TextInputSupport;
import nbbrd.design.StaticFactoryMethod;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Locale;

@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarkdownInputSupport extends TextInputSupport implements DirectoryStream.Filter<Path> {

    @SafeVarargs
    @StaticFactoryMethod
    public static @NonNull MarkdownInputSupport newMarkdownInputSupport(@NonNull CommandSupporter<? super MarkdownInputSupport>... supporters) {
        return CommandSupporter.create(MarkdownInputSupport::new, supporters);
    }

    private @NonNull Parser parser = FlexmarkIO.newParser();

    public Document readDocument(Path file) throws IOException {
        try (Reader reader = newBufferedReader(file)) {
            return parser.parseReader(reader);
        }
    }

    public String getName(Path file) {
        return !isStdInFile(file) ? file.toString() : "stdin";
    }

    @Override
    public boolean accept(Path entry) throws IOException {
        return entry.toString().toLowerCase(Locale.ROOT).endsWith(".md");
    }
}
