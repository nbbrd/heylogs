package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batch = true
)
public interface Rule {

    String getName();

    Failure validate(Node node);

    @ServiceFilter
    boolean isAvailable();

    String ENABLE_KEY = "heylogs.rule.enable";

    static boolean isEnabled(@NonNull Properties properties, @NonNull String ruleName) {
        String list = properties.getProperty(ENABLE_KEY);
        return list != null && Arrays.asList(list.split(",", -1)).contains(ruleName);
    }
}
