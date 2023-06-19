package nbbrd.heylogs.spi;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.Failure;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Properties;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batch = true
)
public interface Rule {

    @NonNull String getId();

    @Nullable Failure validate(@NonNull Node node);

    @ServiceFilter
    boolean isAvailable();

    Failure NO_PROBLEM = null;

    String ENABLE_KEY = "heylogs.rule.enable";

    static boolean isEnabled(@NonNull Properties properties, @NonNull String ruleId) {
        String list = properties.getProperty(ENABLE_KEY);
        return list != null && Arrays.asList(list.split(",", -1)).contains(ruleId);
    }
}