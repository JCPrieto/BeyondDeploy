package es.jklabs.utilidades;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtils {

    private static final String RESOURCE = "app.properties";
    private static final String KEY = "app.version";
    private static String version;

    private VersionUtils() {

    }

    public static String getVersion() {
        if (version == null) {
            version = loadVersion();
        }
        return version;
    }

    static String loadVersion() {
        Properties props = new Properties();
        try (InputStream in = VersionUtils.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in != null) {
                props.load(in);
                String value = props.getProperty(KEY);
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        }
        return "0.0.0";
    }
}
