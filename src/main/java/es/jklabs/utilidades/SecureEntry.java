package es.jklabs.utilidades;

public class SecureEntry {

    private String credentialRef;
    private String nonceB64;
    private String ciphertextB64;

    public SecureEntry() {
    }

    public SecureEntry(String credentialRef, String nonceB64, String ciphertextB64) {
        this();
        this.credentialRef = credentialRef;
        this.nonceB64 = nonceB64;
        this.ciphertextB64 = ciphertextB64;
    }

    public String getCredentialRef() {
        return credentialRef;
    }

    public void setCredentialRef(String credentialRef) {
        this.credentialRef = credentialRef;
    }

    public String getNonceB64() {
        return nonceB64;
    }

    public void setNonceB64(String nonceB64) {
        this.nonceB64 = nonceB64;
    }

    public String getCiphertextB64() {
        return ciphertextB64;
    }

    public void setCiphertextB64(String ciphertextB64) {
        this.ciphertextB64 = ciphertextB64;
    }
}
