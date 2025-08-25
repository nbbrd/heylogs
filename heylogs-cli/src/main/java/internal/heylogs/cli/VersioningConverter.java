package internal.heylogs.cli;

import nbbrd.heylogs.VersioningConfig;
import picocli.CommandLine;

public final class VersioningConverter implements CommandLine.ITypeConverter<VersioningConfig> {

    @Override
    public VersioningConfig convert(String value) throws Exception {
        return VersioningConfig.parse(value);
    }
}
