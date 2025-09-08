package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;

public interface ForgeLink {

    @NonNull
    URL toURL();

    @Nullable
    ForgeRef toRef(@Nullable ForgeRef baseRef);
}
