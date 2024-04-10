package nbbrd.heylogs;

import java.net.URL;
import java.util.List;

@lombok.Value
@lombok.Builder
public class Summary {

    boolean valid;

    @lombok.Builder.Default
    int releaseCount = 0;

    @lombok.Builder.Default
    TimeRange timeRange = TimeRange.ALL;

    @lombok.Singular
    List<String> compatibilities;

    @lombok.Builder.Default
    int unreleasedChanges = 0;

    @lombok.Builder.Default
    String forgeName = null;

    @lombok.Builder.Default
    URL forgeURL = null;
}
