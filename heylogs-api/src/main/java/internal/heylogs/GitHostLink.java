package internal.heylogs;

import lombok.NonNull;

import java.net.URL;

public interface GitHostLink {

    @NonNull URL getBase();
}
