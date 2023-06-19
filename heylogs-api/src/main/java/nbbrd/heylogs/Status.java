package nbbrd.heylogs;

@lombok.Value
@lombok.Builder
public class Status {

    @lombok.Builder.Default
    int releaseCount = 0;

    @lombok.Builder.Default
    TimeRange timeRange = TimeRange.ALL;

    @lombok.Builder.Default
    boolean compatibleWithSemver = false;

    @lombok.Builder.Default
    String semverDetails = "";

    @lombok.Builder.Default
    boolean hasUnreleasedSection = false;
}
