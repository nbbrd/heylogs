package nbbrd.heylogs.spi;

import lombok.NonNull;

public interface ForgeRef {

    boolean isCompatibleWith(@NonNull ForgeLink link);
}
