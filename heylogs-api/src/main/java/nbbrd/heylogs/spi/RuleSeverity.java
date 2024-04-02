package nbbrd.heylogs.spi;

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

    @lombok.Getter
    private final int code;
}
