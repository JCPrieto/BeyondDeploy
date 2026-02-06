package es.jklabs.utilidades;

import java.util.LinkedHashMap;
import java.util.Map;

public class SecureVault {

    private Map<String, SecureEntry> entries = new LinkedHashMap<>();

    public Map<String, SecureEntry> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, SecureEntry> entries) {
        this.entries = entries;
    }
}
