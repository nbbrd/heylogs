package nbbrd.heylogs.spi;

import lombok.NonNull;

public interface ForgeRef<T extends ForgeLink> {

    boolean isCompatibleWith(@NonNull T link);
}
