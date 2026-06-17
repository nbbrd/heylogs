package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.ChangelogContent;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Scan;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;

import java.io.IOException;
import java.io.Reader;
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
    @NonNull
    String getFormatId();

    @NonNull
    String getFormatName();

    @NonNull
    String getFormatModuleId();

    @NonNull
    Set<FormatType> getSupportedFormatTypes();

    void formatProblems(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException, UnsupportedOperationException;

    void formatStatus(@NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException, UnsupportedOperationException;

    void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException, UnsupportedOperationException;

    void formatContent(@NonNull Appendable appendable, @NonNull ChangelogContent content) throws IOException, UnsupportedOperationException;

    @NonNull
    ChangelogContent parseContent(@NonNull Reader reader) throws IOException;

    @NonNull
    DirectoryStream.Filter<? super Path> getFormatFileFilter();
}
