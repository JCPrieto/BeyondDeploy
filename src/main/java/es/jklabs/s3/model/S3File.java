package es.jklabs.s3.model;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3File)) return false;
        S3File s3File = (S3File) o;
        return Objects.equals(getFullPath(), s3File.getFullPath());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getFullPath());
    }
}
