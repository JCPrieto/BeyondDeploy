package es.jklabs.utilidades;

interface MasterKeyProvider {

    SecureStorageManager.ProviderType getType();

    boolean isAvailable();

    byte[] loadKey();

    void storeKey(byte[] key);
}
