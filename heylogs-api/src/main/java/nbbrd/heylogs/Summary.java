package nbbrd.heylogs;

import java.util.List;

@lombok.Value
@lombok.Builder
public class Summary {

    @lombok.Builder.Default
    int releaseCount = 0;

    @lombok.Builder.Default
    TimeRange timeRange = TimeRange.ALL;

    @lombok.Singular
    List<String> compatibilities;

    @lombok.Builder.Default
    boolean hasUnreleasedSection = false;
}
