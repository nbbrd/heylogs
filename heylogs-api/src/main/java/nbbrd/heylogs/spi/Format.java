package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Status;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;

import java.io.IOException;
import java.util.List;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batchType = FormatBatch.class
)
public interface Format {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull String getId();

    void formatFailures(@NonNull Appendable appendable, @NonNull String source, @NonNull List<Failure> failures) throws IOException;

    void formatStatus(@NonNull Appendable appendable, @NonNull String source, @NonNull Status status) throws IOException;

    void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> resources) throws IOException;
}
