package es.jklabs.utilidades;

import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VersionUtilsTest {

    @Test
    public void leeVersionDesdeResources() throws Exception {
        Properties props = new Properties();
        try (InputStream in = VersionUtils.class.getClassLoader().getResourceAsStream("app.properties")) {
            assertNotNull(in);
            props.load(in);
        }
        String expected = props.getProperty("app.version");
        assertNotNull(expected);

        assertEquals(expected, VersionUtils.getVersion());
    }
}
