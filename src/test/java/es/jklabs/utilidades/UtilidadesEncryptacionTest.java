package es.jklabs.utilidades;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilidadesEncryptacionTest {

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
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException expected) {
            // esperado
        }
    }
}
