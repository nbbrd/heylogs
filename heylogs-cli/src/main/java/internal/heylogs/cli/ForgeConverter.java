package internal.heylogs.cli;

import nbbrd.heylogs.ForgeConfig;
import picocli.CommandLine;

public final class ForgeConverter implements CommandLine.ITypeConverter<ForgeConfig> {

    @Override
    public ForgeConfig convert(String value) throws Exception {
        return ForgeConfig.parse(value);
    }
}
