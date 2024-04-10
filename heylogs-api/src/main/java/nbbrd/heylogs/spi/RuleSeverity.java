package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.RepresentableAsInt;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.util.stream.Stream;

//@RepresentableAsString
@RepresentableAsInt(formatMethodName = "toCode", parseMethodName = "parseCode")
@lombok.AllArgsConstructor
public enum RuleSeverity {

    /**
     * Turn the rule off
     */
    OFF(0),

    /**
     * Turn the rule on as a warning
     */
    WARN(1),

    /**
     * Turn the rule on as an error
     */
    ERROR(2);

    private final int code;

    public int toCode() {
        return code;
    }

    @StaticFactoryMethod
    public static @NonNull RuleSeverity parseCode(int code) {
        return Stream.of(values()).filter(value -> value.code == code)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
