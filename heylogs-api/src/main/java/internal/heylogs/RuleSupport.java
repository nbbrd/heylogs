package internal.heylogs;

import com.vladsch.flexmark.ast.LinkNodeBase;
import lombok.NonNull;
import nbbrd.io.text.Parser;

import java.net.URL;
import java.util.Locale;
import java.util.Optional;

public final class RuleSupport {

    private RuleSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull String nameToId(Enum<?> o) {
        return o.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static @NonNull Optional<URL> linkToURL(@NonNull LinkNodeBase link) {
        return Parser.onURL().parseValue(link.getUrl());
    }
}
