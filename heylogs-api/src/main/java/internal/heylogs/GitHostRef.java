package internal.heylogs;

import lombok.NonNull;

public interface GitHostRef<T extends GitHostLink> {

    boolean isCompatibleWith(@NonNull T link);
}
