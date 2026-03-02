package nbbrd.heylogs.enforcer.rules;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import nbbrd.design.MightBePromoted;
import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

import java.io.*;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@lombok.Getter
@lombok.Setter
abstract class HeylogsRule extends AbstractEnforcerRule {

    protected Document readChangelog(File inputFile) throws EnforcerRuleException {
        getLog().info("Reading changelog " + inputFile);
        try {
            return FlexmarkIO.newTextParser().parseFile(inputFile, UTF_8);
        } catch (IOException ex) {
            throw new EnforcerRuleException("Failed to read changelog", ex);
        }
    }

    protected static boolean accept(Path entry) {
        return entry.toString().toLowerCase(Locale.ROOT).endsWith(".md");
    }

    @MightBePromoted
    protected static Writer newWriter(File outputFile, Consumer<CharSequence> logger) throws IOException {
        return newTextOutputSupport().isStdoutFile(outputFile.toPath().getFileName())
                ? new RuleLogWriter(logger)
                : newTextOutputSupport().newBufferedWriter(outputFile.toPath());
    }

    @lombok.AllArgsConstructor
    private static final class RuleLogWriter extends StringWriter {

        private final Consumer<CharSequence> logger;

        @Override
        public void close() throws IOException {
            super.close();
            new BufferedReader(new StringReader(toString())).lines().forEach(logger);
        }
    }
}
