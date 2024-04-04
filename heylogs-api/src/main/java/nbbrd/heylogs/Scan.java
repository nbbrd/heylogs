package nbbrd.heylogs;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class Scan {

    @NonNull String source;

    @NonNull Summary summary;
}
