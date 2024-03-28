package nbbrd.heylogs;

import lombok.NonNull;

@lombok.Value
public class Resource {

    @NonNull String type;
    @NonNull String id;
}
