package internal.heylogs;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public final class Properties2 {

    private Properties2() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Charset PROPERTIES_CHARSET = ISO_8859_1;

    public static Properties loadFromStream(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        return properties;
    }

    public static void storeToStream(Properties properties, OutputStream stream) throws IOException {
        properties.store(stream, "");
    }

    public static Properties loadFromReader(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    public static void storeToWriter(Properties properties, Writer writer) throws IOException {
        properties.store(writer, "");
    }
}
