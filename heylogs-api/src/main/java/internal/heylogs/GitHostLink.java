package internal.heylogs;

import lombok.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

public interface GitHostLink {

    @NonNull URL getBase();

    static @NonNull URL urlOf(@NonNull String text) throws IllegalArgumentException {
        try {
            return new URL(text);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
