package tests.heylogs.spi;

import internal.heylogs.git.ThreeDotDiff;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

public final class ThreeDotDiffConverter extends SimpleArgumentConverter {

    @Override
    protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
        Assertions.assertEquals(ThreeDotDiff.class, targetType, "Can only convert to ThreeDotDiff");
        return source == null ? null : ThreeDotDiff.parse(source.toString());
    }
}
