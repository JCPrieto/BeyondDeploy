package es.jklabs.utilidades;

import java.util.Base64;

class SystemPropertyKeyProvider implements MasterKeyProvider {

    static final String PROPERTY = "beyonddeploy.masterkey.b64";

    @Override
    public SecureStorageManager.ProviderType getType() {
        return SecureStorageManager.ProviderType.SYSTEM_PROPERTY;
    }

    @Override
    public boolean isAvailable() {
        String value = System.getProperty(PROPERTY);
        return value != null && !value.isEmpty();
    }

    @Override
    public byte[] loadKey() {
        String value = System.getProperty(PROPERTY);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(value);
    }

    @Override
    public void storeKey(byte[] key) {
        // No-op: se inyecta por propiedad del sistema.
    }
}
