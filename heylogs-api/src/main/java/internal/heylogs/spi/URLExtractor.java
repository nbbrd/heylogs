package internal.heylogs.spi;

import lombok.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class URLExtractor {

    private URLExtractor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull String[] getPathArray(@NonNull URL url) {
        String path = url.getPath();
        if (path == null || path.isEmpty()) return new String[0];
        String[] result = path.substring(1).split("/", -1);
        for (int i = 0; i < result.length; i++) {
            result[i] = decode(result[i]);
        }
        return result;
    }

    private static String decode(String o) {
        try {
            return URLDecoder.decode(o, UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static @NonNull URL baseOf(@NonNull URL url) {
        try {
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), null, null, null).toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static @NonNull URL urlOf(@NonNull CharSequence text) throws IllegalArgumentException {
        try {
            return URI.create(text.toString()).toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static void checkPathLength(String[] pathArray, int... lengths) {
        if (IntStream.of(lengths).noneMatch(length -> length == pathArray.length)) {
            throw new IllegalArgumentException("Invalid path length: expecting " + Arrays.toString(lengths) + ", found " + pathArray.length);
        }
    }

    public static void checkPathItem(String[] pathArray, int index, String... values) {
        if (!Arrays.asList(values).contains(pathArray[index]))
            throw new IllegalArgumentException("Invalid path item: expecting " + Arrays.toString(values) + ", found '" + pathArray[index] + "'");
    }

    public static void checkPathItem(String[] pathArray, int index, Pattern pattern) {
        if (!pattern.matcher(pathArray[index]).matches())
            throw new IllegalArgumentException("Invalid path item at index " + index + ": expecting pattern '" + pattern.pattern() + "', found '" + pathArray[index] + "'");
    }
}
