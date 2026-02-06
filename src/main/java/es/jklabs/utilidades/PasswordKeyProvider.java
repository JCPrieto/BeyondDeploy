package es.jklabs.utilidades;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

class PasswordKeyProvider implements MasterKeyProvider {

    private static final int DEFAULT_ITERATIONS = 120000;
    private static final int KEY_LENGTH = 32;
    private final SecureMeta meta;

    PasswordKeyProvider(SecureMeta meta) {
        this.meta = meta;
    }

    @Override
    public SecureStorageManager.ProviderType getType() {
        return SecureStorageManager.ProviderType.PASSWORD_PBKDF2;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public byte[] loadKey() {
        ensureMeta();
        char[] password = solicitarPassword();
        try {
            return derivarClave(password, Base64.getDecoder().decode(meta.getSaltB64()), meta.getIterations(), meta.getKeyLength());
        } finally {
            wipeChars(password);
        }
    }

    @Override
    public void storeKey(byte[] key) {
        // No-op: se deriva de la contraseña.
    }

    private void ensureMeta() {
        if (meta.getSaltB64() == null || meta.getSaltB64().isEmpty()) {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            meta.setSaltB64(Base64.getEncoder().encodeToString(salt));
            meta.setIterations(DEFAULT_ITERATIONS);
            meta.setKeyLength(KEY_LENGTH);
            SecureStorageManager.getInstance().saveMeta();
            wipe(salt);
        }
    }

    private char[] solicitarPassword() {
        JPasswordField field = new JPasswordField();
        int opt = JOptionPane.showConfirmDialog(null, field, Mensajes.getMensaje("masterkey.password.titulo"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) {
            throw new IllegalStateException(Mensajes.getError("masterkey.password.cancelada"));
        }
        return field.getPassword();
    }

    private byte[] derivarClave(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("Error al derivar clave PBKDF2", e);
        }
    }

    private void wipe(byte[] buffer) {
        if (buffer != null) {
            Arrays.fill(buffer, (byte) 0);
        }
    }

    private void wipeChars(char[] buffer) {
        if (buffer != null) {
            Arrays.fill(buffer, '\0');
        }
    }
}
