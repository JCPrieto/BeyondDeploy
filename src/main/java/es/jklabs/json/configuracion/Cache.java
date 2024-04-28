package es.jklabs.json.configuracion;

import java.io.Serializable;

public class Cache implements Serializable {
    private static final long serialVersionUID = 6122835970598887510L;
    private String uploadFolder;

    public String getUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(String uploadFolder) {
        this.uploadFolder = uploadFolder;
    }
}
