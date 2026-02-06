package es.jklabs.utilidades;

import java.io.IOException;
import java.util.Base64;

class MacOSKeychainProvider implements MasterKeyProvider {

    private final String service;
    private final String account;

    MacOSKeychainProvider(String service, String account) {
        this.service = service;
        this.account = account;
    }

    @Override
    public SecureStorageManager.ProviderType getType() {
        return SecureStorageManager.ProviderType.MACOS_KEYCHAIN;
    }

    @Override
    public boolean isAvailable() {
        return CommandUtil.osName().contains("mac");
    }

    @Override
    public byte[] loadKey() {
        try {
            CommandUtil.CommandResult result = CommandUtil.runCommand(java.util.List.of("security",
                    "find-generic-password", "-s", service, "-a", account, "-w"), null);
            if (result.exitCode() != 0 || result.stdout() == null || result.stdout().isEmpty()) {
                return null;
            }
            return Base64.getDecoder().decode(result.stdout().trim());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void storeKey(byte[] key) {
        String b64 = Base64.getEncoder().encodeToString(key);
        try {
            CommandUtil.CommandResult result = CommandUtil.runCommand(java.util.List.of("security",
                    "add-generic-password", "-U", "-s", service, "-a", account, "-w", b64), null);
            if (result.exitCode() != 0) {
                throw new IllegalStateException("No se pudo guardar la clave en Keychain: " + result.stderr());
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la clave en Keychain", e);
        }
    }
}
