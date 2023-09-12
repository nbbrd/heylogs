package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.misc.Pair;
import org.eclipse.jgit.annotations.NonNull;
import picocli.CommandLine.ITypeConverter;

public class GitRangeConverter implements ITypeConverter<Pair<String, String>> {
    @Override
    public Pair<String, String> convert(@NonNull String value)  {
        String[] ids = value.split("\\.\\.\\.");
        if (ids.length != 2) {
            throw new IllegalArgumentException("Invalid commit range format! Expected format is id1...id2.");
        }
        return Pair.of(ids[0], ids[1]);
    }
}
