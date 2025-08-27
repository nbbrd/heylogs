package internal.heylogs.cli;

import nbbrd.heylogs.RuleConfig;
import picocli.CommandLine;

public final class RuleConverter implements CommandLine.ITypeConverter<RuleConfig> {

    @Override
    public RuleConfig convert(String value) throws Exception {
        return RuleConfig.parse(value);
    }
}
