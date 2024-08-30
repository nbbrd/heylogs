package internal.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.console.picocli.CommandSupporter;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.design.StaticFactoryMethod;

import java.io.IOException;
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

    public void writeDocument(Path file, Document document) throws IOException {
        FlexmarkIO.newTextFormatter().formatWriter(document, () -> newBufferedWriter(file));
    }
}
