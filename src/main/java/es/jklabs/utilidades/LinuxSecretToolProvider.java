package es.jklabs.utilidades;

import java.io.IOException;
import java.util.Base64;

class LinuxSecretToolProvider implements MasterKeyProvider {

    private final String service;
    private final String account;
    private final String secretTool;

    LinuxSecretToolProvider(String service, String account) {
        this.service = service;
        this.account = account;
        this.secretTool = CommandUtil.findCommand();
    }

    @Override
    public SecureStorageManager.ProviderType getType() {
        return SecureStorageManager.ProviderType.LINUX_SECRET_TOOL;
    }

    @Override
    public boolean isAvailable() {
        if (!CommandUtil.osName().contains("linux")) {
            return false;
        }
        try {
            if (secretTool == null || secretTool.isEmpty()) {
                return false;
            }
            CommandUtil.CommandResult result = CommandUtil.runCommand(java.util.List.of(secretTool, "--version"), null);
            if (result.exitCode() == 0) {
                return true;
            }
            String combined = (result.stdout() + "\n" + result.stderr()).toLowerCase();
            return combined.contains("usage: secret-tool");
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public byte[] loadKey() {
        try {
            if (secretTool == null || secretTool.isEmpty()) {
                return null;
            }
            CommandUtil.CommandResult lookup = CommandUtil.runCommand(java.util.List.of(secretTool, "lookup",
                    "service", service, "account", account), null);
            if (lookup.exitCode() != 0 || lookup.stdout() == null || lookup.stdout().isEmpty()) {
                return null;
            }
            return Base64.getDecoder().decode(lookup.stdout().trim());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void storeKey(byte[] key) {
        String b64 = Base64.getEncoder().encodeToString(key);
        try {
            String label = service + " master key";
            if (secretTool == null || secretTool.isEmpty()) {
                throw new IllegalStateException("secret-tool no encontrado");
            }
            CommandUtil.CommandResult store = CommandUtil.runCommand(java.util.List.of(secretTool, "store",
                            "--label", label, "service", service, "account", account),
                    (b64 + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            if (store.exitCode() != 0) {
                throw new IllegalStateException("No se pudo guardar la clave en secret-tool: " + store.stderr());
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la clave en secret-tool", e);
        }
    }
}
