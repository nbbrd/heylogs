package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRef;

@lombok.Value(staticConstructor = "of")
public class MockedForgeRef implements ForgeRef {

    boolean compatibility;

    @Override
    public boolean isCompatibleWith(@NonNull ForgeLink link) {
        return compatibility;
    }
}
