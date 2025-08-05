package tests.heylogs.spi;

import internal.heylogs.git.Hash;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

public final class HashConverter extends SimpleArgumentConverter {

    @Override
    protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
        Assertions.assertEquals(Hash.class, targetType, "Can only convert to Hash");
        return source == null ? null : Hash.parse(source.toString());
    }
}
