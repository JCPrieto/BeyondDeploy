package es.jklabs.utilidades;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class UtilidadesEncryptacion {

    private static final Logger LOG = Logger.getLogger();
    private static final String INIT_VECTOR = "43&pH#6A8H*w4zLN";
    private static final String KEY = "Y6+RcNdb&&9Wf!V9";
    private static final String UTF_8 = "UTF-8";

    private UtilidadesEncryptacion() {

    }

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return DatatypeConverter.printBase64Binary(encrypted);
        } catch (Exception ex) {
            LOG.error("Encriptar dato", ex);
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(encrypted));

            return new String(original);
        } catch (Exception ex) {
            LOG.error("Desencriptar dato", ex);
        }

        return null;
    }
}
