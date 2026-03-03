package es.jklabs.utilidades;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

class WindowsCredentialManagerProvider implements MasterKeyProvider {

    private static final int CREDENTIAL_TYPE_GENERIC = 1;
    private static final int CREDENTIAL_PERSIST_LOCAL_MACHINE = 2;
    private static volatile boolean apiChecked;
    private static volatile boolean apiAvailable;
    private final String targetName;
    private final String userName;

    WindowsCredentialManagerProvider(String service, String account) {
        this.targetName = service + ":" + account;
        this.userName = account;
    }

    @Override
    public SecureStorageManager.ProviderType getType() {
        return SecureStorageManager.ProviderType.WINDOWS_CREDENTIAL_MANAGER;
    }

    private static boolean isCredentialApiAvailable() {
        if (apiChecked) {
            return apiAvailable;
        }
        synchronized (WindowsCredentialManagerProvider.class) {
            if (apiChecked) {
                return apiAvailable;
            }
            try {
                NativeLibrary lib = NativeLibrary.getInstance("Advapi32");
                lib.getFunction("CredReadW");
                lib.getFunction("CredWriteW");
                lib.getFunction("CredFree");
                apiAvailable = true;
            } catch (Throwable t) {
                apiAvailable = false;
                Logger.info("Windows Credential Manager no disponible: " + t.getMessage());
            }
            apiChecked = true;
            return apiAvailable;
        }
    }

    @Override
    public boolean isAvailable() {
        return CommandUtil.osName().startsWith("win") && isCredentialApiAvailable();
    }

    @Override
    public byte[] loadKey() {
        if (!isCredentialApiAvailable()) {
            return null;
        }
        PointerByReference pcred = new PointerByReference();
        boolean ok = Advapi32Cred.INSTANCE.CredReadW(new WString(targetName), CREDENTIAL_TYPE_GENERIC, 0, pcred);
        if (!ok) {
            return null;
        }
        CREDENTIAL cred = new CREDENTIAL(pcred.getValue());
        cred.read();
        byte[] blob = cred.CredentialBlob.getByteArray(0, cred.CredentialBlobSize);
        Advapi32Cred.INSTANCE.CredFree(pcred.getValue());
        String b64 = new String(blob, StandardCharsets.UTF_16LE);
        return Base64.getDecoder().decode(b64);
    }

    @Override
    public void storeKey(byte[] key) {
        if (!isCredentialApiAvailable()) {
            return;
        }
        String b64 = Base64.getEncoder().encodeToString(key);
        byte[] data = b64.getBytes(StandardCharsets.UTF_16LE);
        CREDENTIAL cred = new CREDENTIAL();
        cred.Type = CREDENTIAL_TYPE_GENERIC;
        cred.TargetName = new WString(targetName);
        cred.UserName = new WString(userName);
        cred.CredentialBlobSize = data.length;
        Memory mem = new Memory(data.length);
        mem.write(0, data, 0, data.length);
        cred.CredentialBlob = mem;
        cred.Persist = CREDENTIAL_PERSIST_LOCAL_MACHINE;
        cred.write();
        Advapi32Cred.INSTANCE.CredWriteW(cred, 0);
    }

    interface Advapi32Cred extends Library {
        Advapi32Cred INSTANCE = Native.load("Advapi32", Advapi32Cred.class, W32APIOptions.UNICODE_OPTIONS);

        boolean CredReadW(WString targetName, int type, int flags, PointerByReference pcredential);

        boolean CredWriteW(CREDENTIAL credential, int flags);

        void CredFree(Pointer cred);
    }

    public static class CREDENTIAL extends Structure {
        public int Flags;
        public int Type;
        public WString TargetName;
        public WString Comment;
        public long LastWritten;
        public int CredentialBlobSize;
        public Pointer CredentialBlob;
        public int Persist;
        public int AttributeCount;
        public Pointer Attributes;
        public WString TargetAlias;
        public WString UserName;

        public CREDENTIAL() {
        }

        public CREDENTIAL(Pointer p) {
            super(p);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("Flags", "Type", "TargetName", "Comment", "LastWritten", "CredentialBlobSize",
                    "CredentialBlob", "Persist", "AttributeCount", "Attributes", "TargetAlias", "UserName");
        }
    }
}
