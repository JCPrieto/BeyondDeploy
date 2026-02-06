package es.jklabs.utilidades;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

public class UtilidadesEncryptacion {

    private static final String LEGACY_KEY_ENV = "BEYONDDEPLOY_LEGACY_KEY_B64";
    private static final String LEGACY_IV_ENV = "BEYONDDEPLOY_LEGACY_IV_B64";
    // Solo para migración legacy: mantener hardcodeadas las claves anteriores.
    private static final String LEGACY_KEY_FALLBACK_B64 = "WTYrUmNOZGImJjlXZiFWOQ==";
    private static final String LEGACY_IV_FALLBACK_B64 = "NDMmcEgjNkE4SCp3NHpMTg==";

    private UtilidadesEncryptacion() {

    }

    public static String encrypt(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Texto a cifrar null");
        }
        SecureStorageManager.getInstance().ensureProviderAvailable(null);
        return SecureStorageManager.getInstance().storeSecret(value);
    }

    public static String decrypt(String encrypted) {
        return decrypt(encrypted, null);
    }

    public static String decrypt(String encrypted, Consumer<String> onMigratedRef) {
        if (encrypted == null) {
            throw new IllegalArgumentException("Texto a descifrar null");
        }
        SecureStorageManager.getInstance().ensureProviderAvailable(null);
        if (encrypted.startsWith(SecureStorageManager.REF_PREFIX)) {
            return SecureStorageManager.getInstance().retrieveSecret(encrypted);
        }
        String legacy = legacyDecrypt(encrypted);
        if (legacy != null) {
            SecureStorageManager storage = SecureStorageManager.getInstance();
            if (!storage.isMigrationNotified()) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        Mensajes.getMensaje("almacenamiento.seguro.migracion.aviso"),
                        Mensajes.getMensaje("almacenamiento.seguro.titulo"),
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                storage.markMigrationNotified();
            }
            String newRef = SecureStorageManager.getInstance().storeSecret(legacy);
            if (onMigratedRef != null) {
                onMigratedRef.accept(newRef);
            }
            return legacy;
        }
        throw new IllegalStateException("Error al desencriptar dato");
    }

    private static String legacyDecrypt(String encrypted) {
        String keyB64 = System.getenv(LEGACY_KEY_ENV);
        String ivB64 = System.getenv(LEGACY_IV_ENV);
        if (keyB64 == null || keyB64.isEmpty()) {
            keyB64 = LEGACY_KEY_FALLBACK_B64;
        }
        if (ivB64 == null || ivB64.isEmpty()) {
            ivB64 = LEGACY_IV_FALLBACK_B64;
        }
        byte[] key = Base64.getDecoder().decode(keyB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            String value = new String(original, StandardCharsets.UTF_8);
            wipe(original);
            return value;
        } catch (Exception ex) {
            Logger.error("Desencriptar dato", ex);
            return null;
        } finally {
            wipe(key);
            wipe(iv);
        }
    }

    private static void wipe(byte[] buffer) {
        if (buffer != null) {
            java.util.Arrays.fill(buffer, (byte) 0);
        }
    }
}
