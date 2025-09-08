package internal.heylogs.cli;

import nbbrd.heylogs.TaggingConfig;
import picocli.CommandLine;

public final class TaggingConverter implements CommandLine.ITypeConverter<TaggingConfig> {

    @Override
    public TaggingConfig convert(String value) throws Exception {
        return TaggingConfig.parse(value);
    }
}
