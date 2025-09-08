package internal.heylogs.cli;

import nbbrd.heylogs.DomainConfig;
import picocli.CommandLine;

public final class DomainConverter implements CommandLine.ITypeConverter<DomainConfig> {

    @Override
    public DomainConfig convert(String value) throws Exception {
        return DomainConfig.parse(value);
    }
}
