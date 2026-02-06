package es.jklabs.utilidades;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.Assert.*;

public class UtilidadesEncryptacionTest {

    @Before
    public void setup() throws Exception {
        Path tempDir = Files.createTempDirectory("bd-secure");
        System.setProperty("beyonddeploy.storage.dir", tempDir.toString());
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        System.setProperty("beyonddeploy.masterkey.b64", Base64.getEncoder().encodeToString(key));
        SecureStorageManager.resetForTest();
    }

    @After
    public void cleanup() {
        SecureStorageManager.resetForTest();
        System.clearProperty("beyonddeploy.storage.dir");
        System.clearProperty("beyonddeploy.masterkey.b64");
    }

    @Test
    public void encryptDecryptRoundtrip() {
        String original = "secreto-123";

        String encrypted = UtilidadesEncryptacion.encrypt(original);

        assertNotNull(encrypted);
        assertEquals(original, UtilidadesEncryptacion.decrypt(encrypted));
    }

    @Test
    public void decryptConBase64InvalidoLanzaExcepcion() {
        try {
            UtilidadesEncryptacion.decrypt("###no-base64###");
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException expected) {
            // esperado
        }
    }

    @Test
    public void encryptConNullLanzaExcepcion() {
        try {
            UtilidadesEncryptacion.encrypt(null);
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // esperado
        }
    }
}
