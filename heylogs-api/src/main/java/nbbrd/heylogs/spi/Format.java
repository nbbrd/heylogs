package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Failure;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

import java.io.IOException;
import java.util.List;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batch = true
)
public interface Format {

    @NonNull String getId();

    void formatFailures(@NonNull Appendable appendable, @NonNull String source, @NonNull List<Failure> failures) throws IOException;
}
