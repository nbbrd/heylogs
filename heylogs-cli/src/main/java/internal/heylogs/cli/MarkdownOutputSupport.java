package internal.heylogs.cli;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.util.ast.Document;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.console.picocli.CommandSupporter;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.design.StaticFactoryMethod;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarkdownOutputSupport extends TextOutputSupport {

    @SafeVarargs
    @StaticFactoryMethod
    public static @NonNull MarkdownOutputSupport newMarkdownOutputSupport(@NonNull CommandSupporter<? super MarkdownOutputSupport>... supporters) {
        return CommandSupporter.create(MarkdownOutputSupport::new, supporters);
    }

    private @NonNull Formatter formatter = Formatter.builder().build();

    public void writeDocument(Path file, Document document) throws IOException {
        try (Writer writer = newBufferedWriter(file)) {
            formatter.render(document, writer);
        }
    }
}
