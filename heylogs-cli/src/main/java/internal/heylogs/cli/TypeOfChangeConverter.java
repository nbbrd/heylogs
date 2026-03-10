package internal.heylogs.cli;

import nbbrd.heylogs.TypeOfChange;
import picocli.CommandLine;

import java.util.Locale;

public final class TypeOfChangeConverter implements CommandLine.ITypeConverter<TypeOfChange> {

    @Override
    public TypeOfChange convert(String value) {
        return TypeOfChange.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
