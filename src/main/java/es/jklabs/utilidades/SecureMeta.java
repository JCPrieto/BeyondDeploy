package es.jklabs.utilidades;

public class SecureMeta {

    private int version = 1;
    private String provider;
    private String saltB64;
    private int iterations;
    private int keyLength;
    private String keychainService;
    private String keychainAccount;
    private boolean migrationNotified;
    private java.util.List<ProviderConfig> providerConfigs;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSaltB64() {
        return saltB64;
    }

    public void setSaltB64(String saltB64) {
        this.saltB64 = saltB64;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public String getKeychainService() {
        return keychainService;
    }

    public void setKeychainService(String keychainService) {
        this.keychainService = keychainService;
    }

    public String getKeychainAccount() {
        return keychainAccount;
    }

    public void setKeychainAccount(String keychainAccount) {
        this.keychainAccount = keychainAccount;
    }

    public boolean isMigrationNotified() {
        return migrationNotified;
    }

    public void setMigrationNotified(boolean migrationNotified) {
        this.migrationNotified = migrationNotified;
    }

    public java.util.List<ProviderConfig> getProviderConfigs() {
        return providerConfigs;
    }

    public void setProviderConfigs(java.util.List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    public static class ProviderConfig {
        private String type;
        private boolean enabled;
        private int priority;

        public ProviderConfig() {
        }

        public ProviderConfig(String type, boolean enabled, int priority) {
            this();
            this.type = type;
            this.enabled = enabled;
            this.priority = priority;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }
}
