package nbbrd.heylogs;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class Resource {

    @NonNull String type;
    @NonNull String category;
    @NonNull String id;
    @NonNull String name;
}
