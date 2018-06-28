package es.jklabs.s3.model;

import java.io.Serializable;

public class S3File implements Serializable {

    private static final long serialVersionUID = -3045077411639166434L;
    private final String fullPath;
    private String name;

    public S3File(String name, String fullPath) {
        this.name = name;
        this.fullPath = fullPath;
    }

    public String getName() {
        return name;
    }

    public String getFullPath() {
        return fullPath;
    }
}
