package es.jklabs.s3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class S3Folder implements Serializable {

    private static final long serialVersionUID = -4932960673157161234L;
    private String fullpath;
    private List<S3Folder> s3Forlders;
    private String name;
    private List<S3File> s3Files;

    public S3Folder() {
        this.s3Forlders = new ArrayList<>();
        this.s3Files = new ArrayList<>();
        this.fullpath = "";
    }

    private S3Folder(String name, String fullpath) {
        this();
        this.name = name;
        this.fullpath = fullpath;
    }

    public List<S3Folder> getS3Forlders() {
        return s3Forlders;
    }

    public String getName() {
        return name;
    }

    public List<S3File> getS3Files() {
        return s3Files;
    }

    public String getFullpath() {
        return fullpath;
    }

    public void addCarpetas(String carpeta, String fullpath) {
        boolean existeCarpeta = false;
        for (S3Folder s3Folder : getS3Forlders()) {
            if (Objects.equals(s3Folder.getName(), carpeta)) {
                existeCarpeta = true;
                break;
            }
        }
        if (!existeCarpeta) {
            S3Folder nueva = new S3Folder(carpeta, fullpath);
            getS3Forlders().add(nueva);
        }
    }
}
