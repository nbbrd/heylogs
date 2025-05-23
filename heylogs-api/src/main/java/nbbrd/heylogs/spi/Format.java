package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Scan;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batchType = FormatBatch.class
)
public interface Format {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull String getFormatId();

    @NonNull String getFormatName();

    @NonNull String getFormatCategory();

    @NonNull Set<FormatType> getSupportedFormatTypes();

    void formatProblems(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException;

    void formatStatus(@NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException;

    void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException;

    @NonNull DirectoryStream.Filter<? super Path> getFormatFileFilter();
}
