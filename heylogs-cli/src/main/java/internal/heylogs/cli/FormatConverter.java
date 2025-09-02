package internal.heylogs.cli;

import nbbrd.heylogs.FormatConfig;
import picocli.CommandLine;

public final class FormatConverter implements CommandLine.ITypeConverter<FormatConfig> {

    @Override
    public FormatConfig convert(String value) throws Exception {
        return FormatConfig.parse(value);
    }
}
