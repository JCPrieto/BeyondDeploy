package es.jklabs.utilidades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;

public class SecureStorageManager {

    static final String REF_PREFIX = "ref:";
    private static final String VAULT_FILE = "credentials-config.json";
    private static final String META_FILE = "secure-meta.json";
    private static final int NONCE_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_LEN = 32;
    private static final String SERVICE = "BeyondDeploy";
    private static SecureStorageManager instance;
    private final ObjectMapper mapper;
    private final Path baseDir;
    private final Path vaultPath;
    private final Path metaPath;
    private SecureMeta meta;
    private MasterKeyProvider provider;
    private byte[] cachedMasterKey;
    private boolean promptShown;
    private SecureStorageManager() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.baseDir = resolveBaseDir();
        this.vaultPath = baseDir.resolve(VAULT_FILE);
        this.metaPath = baseDir.resolve(META_FILE);
        ensureBaseDir();
        loadMeta();
        this.provider = selectProvider();
    }

    public static synchronized SecureStorageManager getInstance() {
        if (instance == null) {
            instance = new SecureStorageManager();
        }
        return instance;
    }

    static synchronized void resetForTest() {
        if (instance != null) {
            instance.clearCache();
            instance = null;
        }
    }

    public synchronized String storeSecret(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Texto a cifrar null");
        }
        String ref = UUID.randomUUID().toString();
        byte[] nonce = new byte[NONCE_LEN];
        new SecureRandom().nextBytes(nonce);
        byte[] key = getMasterKey();
        byte[] cipher = null;
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        try {
            cipher = encrypt(key, nonce, ref, plainBytes);
            SecureVault vault = loadVault();
            SecureEntry entry = new SecureEntry(ref, Base64.getEncoder().encodeToString(nonce),
                    Base64.getEncoder().encodeToString(cipher));
            vault.getEntries().put(ref, entry);
            saveVault(vault);
            return REF_PREFIX + ref;
        } finally {
            wipe(key);
            wipe(nonce);
            wipe(cipher);
            wipe(plainBytes);
        }
    }

    public synchronized String retrieveSecret(String refOrToken) {
        if (refOrToken == null) {
            throw new IllegalArgumentException("Referencia null");
        }
        String ref = refOrToken.startsWith(REF_PREFIX) ? refOrToken.substring(REF_PREFIX.length()) : refOrToken;
        SecureVault vault = loadVault();
        SecureEntry entry = vault.getEntries().get(ref);
        if (entry == null) {
            throw new IllegalStateException("No existe la referencia en el vault");
        }
        byte[] key = getMasterKey();
        byte[] nonce = Base64.getDecoder().decode(entry.getNonceB64());
        byte[] cipher = Base64.getDecoder().decode(entry.getCiphertextB64());
        byte[] plain = null;
        try {
            plain = decrypt(key, nonce, ref, cipher);
            return new String(plain, StandardCharsets.UTF_8);
        } finally {
            wipe(key);
            wipe(nonce);
            wipe(cipher);
            wipe(plain);
        }
    }

    public synchronized void rotatePassword() {
        if (provider == null || provider.getType() != ProviderType.PASSWORD_PBKDF2) {
            setProviderInternal(ProviderType.PASSWORD_PBKDF2);
        }
        SecureVault vault = loadVault();
        Map<String, SecureEntry> entries = vault.getEntries();
        if (entries.isEmpty()) {
            clearCache();
            return;
        }
        List<ReencryptItem> plaintexts = new ArrayList<>();
        for (SecureEntry entry : entries.values()) {
            String ref = entry.getCredentialRef();
            String plaintext = retrieveSecret(REF_PREFIX + ref);
            plaintexts.add(new ReencryptItem(ref, plaintext));
        }
        meta.setSaltB64(null);
        saveMeta();
        clearCache();
        SecureVault nuevo = new SecureVault();
        for (ReencryptItem item : plaintexts) {
            String ref = item.ref;
            byte[] nonce = new byte[NONCE_LEN];
            new SecureRandom().nextBytes(nonce);
            byte[] key = getMasterKey();
            byte[] cipher = null;
            byte[] plainBytes = item.plaintext.getBytes(StandardCharsets.UTF_8);
            try {
                cipher = encrypt(key, nonce, ref, plainBytes);
                SecureEntry entry = new SecureEntry(ref, Base64.getEncoder().encodeToString(nonce),
                        Base64.getEncoder().encodeToString(cipher));
                nuevo.getEntries().put(ref, entry);
            } finally {
                wipe(key);
                wipe(nonce);
                wipe(cipher);
                wipe(plainBytes);
            }
        }
        saveVault(nuevo);
    }

    public synchronized void ensureProviderAvailable(Component parent) {
        if (promptShown) {
            return;
        }
        // En tests/CI se inyecta la clave por propiedad del sistema; no debe degradarse a prompt UI.
        if (provider != null && provider.getType() == ProviderType.SYSTEM_PROPERTY && provider.isAvailable()) {
            return;
        }
        resolveProviderFromConfig();
        if (hasOsProviderAvailable()) {
            resolveProviderFromConfig();
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            ensurePasswordEnabled();
            resolveProviderFromConfig();
            return;
        }
        promptShown = true;
        while (true) {
            int opt = JOptionPane.showOptionDialog(parent,
                    Mensajes.getMensaje(osProviderMissingKey()),
                    Mensajes.getMensaje("almacenamiento.seguro.titulo"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    new Object[]{Mensajes.getMensaje("almacenamiento.seguro.reintentar"),
                            Mensajes.getMensaje("almacenamiento.seguro.continuar")},
                    Mensajes.getMensaje("almacenamiento.seguro.reintentar"));
            if (opt == JOptionPane.YES_OPTION) {
                if (hasOsProviderAvailable()) {
                    resolveProviderFromConfig();
                    break;
                }
            } else {
                ensurePasswordEnabled();
                resolveProviderFromConfig();
                break;
            }
        }
    }

    public synchronized void clearCachePublic() {
        clearCache();
    }

    public List<ProviderInfo> getProviderInfo() {
        List<ProviderInfo> providers = new ArrayList<>();
        for (SecureMeta.ProviderConfig cfg : getProviderConfigs()) {
            ProviderType type = ProviderType.valueOf(cfg.getType());
            providers.add(buildInfo(type, cfg.getPriority(), cfg.isEnabled()));
        }
        return providers;
    }

    private ProviderInfo buildInfo(ProviderType type, int priority, boolean enabled) {
        MasterKeyProvider p = buildProvider(type);
        boolean available = p.isAvailable();
        boolean active = provider != null && provider.getType() == type;
        return new ProviderInfo(type, available, active, priority, enabled);
    }

    void saveMeta() {
        try {
            mapper.writeValue(metaPath.toFile(), meta);
        } catch (IOException e) {
            Logger.error("Guardar metadata segura", e);
        }
    }

    public synchronized boolean isMigrationNotified() {
        return meta != null && meta.isMigrationNotified();
    }

    public synchronized void markMigrationNotified() {
        if (meta != null && !meta.isMigrationNotified()) {
            meta.setMigrationNotified(true);
            saveMeta();
        }
    }

    private void setProviderInternal(ProviderType type) {
        this.provider = buildProvider(type);
        this.meta.setProvider(type.name());
        saveMeta();
        clearCache();
    }

    private void clearCache() {
        wipe(cachedMasterKey);
        cachedMasterKey = null;
    }

    private MasterKeyProvider selectProvider() {
        MasterKeyProvider system = new SystemPropertyKeyProvider();
        if (system.isAvailable()) {
            meta.setProvider(ProviderType.SYSTEM_PROPERTY.name());
            return system;
        }
        resolveProviderFromConfig();
        if (provider != null) {
            return provider;
        }
        setProviderInternal(ProviderType.PASSWORD_PBKDF2);
        return provider;
    }

    public synchronized UpdateResult updateProviderConfigInternal(ProviderType type, boolean enabled, int priority) {
        SecureMeta.ProviderConfig cfg = findProviderConfig(type);
        if (cfg == null) {
            return new UpdateResult(false, false, "almacenamiento.seguro.validacion.config.no.existe");
        }
        if (!enabled && wouldDisableAll(type)) {
            return new UpdateResult(false, false, "almacenamiento.seguro.validacion.almenos.uno");
        }
        cfg.setEnabled(enabled);
        cfg.setPriority(priority);
        saveMeta();
        resolveProviderFromConfig();
        return new UpdateResult(true, hasPriorityTie(), null);
    }

    public synchronized List<SecureMeta.ProviderConfig> getProviderConfigs() {
        if (meta.getProviderConfigs() == null || meta.getProviderConfigs().isEmpty()) {
            List<SecureMeta.ProviderConfig> defaults = new ArrayList<>();
            if (CommandUtil.osName().contains("linux")) {
                defaults.add(new SecureMeta.ProviderConfig(ProviderType.LINUX_SECRET_TOOL.name(), true, 100));
            }
            if (CommandUtil.osName().startsWith("win")) {
                defaults.add(new SecureMeta.ProviderConfig(ProviderType.WINDOWS_CREDENTIAL_MANAGER.name(), true, 100));
            }
            if (CommandUtil.osName().contains("mac")) {
                defaults.add(new SecureMeta.ProviderConfig(ProviderType.MACOS_KEYCHAIN.name(), true, 100));
            }
            defaults.add(new SecureMeta.ProviderConfig(ProviderType.PASSWORD_PBKDF2.name(), true, 10));
            meta.setProviderConfigs(defaults);
            saveMeta();
        }
        return meta.getProviderConfigs();
    }

    private void resolveProviderFromConfig() {
        SecureMeta.ProviderConfig best = null;
        for (SecureMeta.ProviderConfig cfg : getProviderConfigs()) {
            if (!cfg.isEnabled()) {
                continue;
            }
            ProviderType type = ProviderType.valueOf(cfg.getType());
            MasterKeyProvider p = buildProvider(type);
            if (!p.isAvailable()) {
                continue;
            }
            if (best == null || cfg.getPriority() > best.getPriority()) {
                best = cfg;
            }
        }
        if (best != null) {
            setProviderInternal(ProviderType.valueOf(best.getType()));
        }
    }

    private SecureMeta.ProviderConfig findProviderConfig(ProviderType type) {
        for (SecureMeta.ProviderConfig cfg : getProviderConfigs()) {
            if (cfg.getType().equals(type.name())) {
                return cfg;
            }
        }
        return null;
    }

    private boolean wouldDisableAll(ProviderType type) {
        for (SecureMeta.ProviderConfig cfg : getProviderConfigs()) {
            if (!cfg.getType().equals(type.name()) && cfg.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPriorityTie() {
        int max = Integer.MIN_VALUE;
        int count = 0;
        for (SecureMeta.ProviderConfig cfg : getProviderConfigs()) {
            if (!cfg.isEnabled()) {
                continue;
            }
            ProviderType type = ProviderType.valueOf(cfg.getType());
            MasterKeyProvider p = buildProvider(type);
            if (!p.isAvailable()) {
                continue;
            }
            if (cfg.getPriority() > max) {
                max = cfg.getPriority();
                count = 1;
            } else if (cfg.getPriority() == max) {
                count += 1;
            }
        }
        return count > 1;
    }

    private void ensurePasswordEnabled() {
        SecureMeta.ProviderConfig cfg = findProviderConfig(ProviderType.PASSWORD_PBKDF2);
        if (cfg != null && !cfg.isEnabled()) {
            cfg.setEnabled(true);
            saveMeta();
        }
    }

    private boolean hasOsProviderAvailable() {
        if (CommandUtil.osName().startsWith("win")) {
            return new WindowsCredentialManagerProvider(SERVICE, account()).isAvailable();
        }
        if (CommandUtil.osName().contains("mac")) {
            return new MacOSKeychainProvider(SERVICE, account()).isAvailable();
        }
        if (CommandUtil.osName().contains("linux")) {
            return new LinuxSecretToolProvider(SERVICE, account()).isAvailable();
        }
        return false;
    }

    private String osProviderMissingKey() {
        if (CommandUtil.osName().startsWith("win")) {
            return "almacenamiento.seguro.proveedor.no.disponible.windows";
        }
        if (CommandUtil.osName().contains("mac")) {
            return "almacenamiento.seguro.proveedor.no.disponible.macos";
        }
        return "almacenamiento.seguro.proveedor.no.disponible.linux";
    }

    private MasterKeyProvider buildProvider(ProviderType type) {
        return switch (type) {
            case WINDOWS_CREDENTIAL_MANAGER -> new WindowsCredentialManagerProvider(SERVICE, account());
            case MACOS_KEYCHAIN -> new MacOSKeychainProvider(SERVICE, account());
            case LINUX_SECRET_TOOL -> new LinuxSecretToolProvider(SERVICE, account());
            case PASSWORD_PBKDF2 -> new PasswordKeyProvider(meta);
            case SYSTEM_PROPERTY -> new SystemPropertyKeyProvider();
        };
    }

    private String account() {
        String user = System.getProperty("user.name");
        return user != null ? user : "default";
    }

    private byte[] getMasterKey() {
        if (cachedMasterKey != null) {
            return cachedMasterKey.clone();
        }
        byte[] key = provider.loadKey();
        if (key == null || key.length == 0) {
            key = generarClave();
            provider.storeKey(key);
        }
        cachedMasterKey = key.clone();
        return key;
    }

    private byte[] generarClave() {
        byte[] key = new byte[KEY_LEN];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private SecureVault loadVault() {
        if (!Files.exists(vaultPath)) {
            return new SecureVault();
        }
        try {
            SecureVault vault = mapper.readValue(vaultPath.toFile(), SecureVault.class);
            if (vault.getEntries() == null) {
                vault.setEntries(new java.util.LinkedHashMap<>());
            }
            return vault;
        } catch (IOException e) {
            Logger.error("Leer vault seguro", e);
            return new SecureVault();
        }
    }

    private void saveVault(SecureVault vault) {
        try {
            mapper.writeValue(vaultPath.toFile(), vault);
        } catch (IOException e) {
            Logger.error("Guardar vault seguro", e);
        }
    }

    private void loadMeta() {
        if (!Files.exists(metaPath)) {
            meta = new SecureMeta();
            return;
        }
        try {
            meta = mapper.readValue(metaPath.toFile(), SecureMeta.class);
        } catch (IOException e) {
            Logger.error("Leer metadata segura", e);
            meta = new SecureMeta();
        }
    }

    private Path resolveBaseDir() {
        String override = System.getProperty("beyonddeploy.storage.dir");
        if (override != null && !override.isEmpty()) {
            return new File(override).toPath();
        }
        return Path.of(UtilidadesFichero.HOME + UtilidadesFichero.SEPARADOR + UtilidadesFichero.BEYOND_DEPLOY_FOLDER);
    }

    private void ensureBaseDir() {
        if (!Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
            } catch (IOException e) {
                Logger.error("Crear carpeta base", e);
            }
        }
    }

    private byte[] encrypt(byte[] key, byte[] nonce, String aad, byte[] plain) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            return cipher.doFinal(plain);
        } catch (Exception e) {
            throw new IllegalStateException("Error cifrando datos", e);
        }
    }

    private byte[] decrypt(byte[] key, byte[] nonce, String aad, byte[] cipherText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new IllegalStateException("Error descifrando datos", e);
        }
    }

    private void wipe(byte[] buffer) {
        if (buffer != null) {
            java.util.Arrays.fill(buffer, (byte) 0);
        }
    }

    public enum ProviderType {
        WINDOWS_CREDENTIAL_MANAGER,
        MACOS_KEYCHAIN,
        LINUX_SECRET_TOOL,
        PASSWORD_PBKDF2,
        SYSTEM_PROPERTY
    }

    private record ReencryptItem(String ref, String plaintext) {
    }

    public static class ProviderInfo {
        private final ProviderType type;
        private final boolean available;
        private final boolean active;
        private final int priority;
        private final boolean enabled;

        private ProviderInfo(ProviderType type, boolean available, boolean active, int priority, boolean enabled) {
            this.type = type;
            this.available = available;
            this.active = active;
            this.priority = priority;
            this.enabled = enabled;
        }

        public ProviderType getType() {
            return type;
        }

        public boolean isAvailable() {
            return available;
        }

        public boolean isActive() {
            return active;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class UpdateResult {
        private final boolean applied;
        private final boolean priorityTie;
        private final String errorKey;

        private UpdateResult(boolean applied, boolean priorityTie, String errorKey) {
            this.applied = applied;
            this.priorityTie = priorityTie;
            this.errorKey = errorKey;
        }

        public boolean isApplied() {
            return applied;
        }

        public boolean hasPriorityTie() {
            return priorityTie;
        }

        public String getErrorKey() {
            return errorKey;
        }
    }
}
