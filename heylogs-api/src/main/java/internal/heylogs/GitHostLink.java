package internal.heylogs;

import lombok.NonNull;

public interface GitHostLink {

    @NonNull String getProtocol();

    @NonNull String getHost();

    int getPort();

    int NO_PORT = -1;
}
