package es.jklabs.s3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class S3Folder implements Serializable {

    private static final long serialVersionUID = -4932960673157161234L;
    private List<S3Folder> s3Forlders;
    private String name;
    private List<S3File> s3Files;

    public S3Folder() {
        this.s3Forlders = new ArrayList<>();
        this.s3Files = new ArrayList<>();
    }

    public S3Folder(String name) {
        this();
        this.name = name;
    }

    public List<S3Folder> getS3Forlders() {
        return s3Forlders;
    }

    public void setS3Forlders(List<S3Folder> s3Forlders) {
        this.s3Forlders = s3Forlders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<S3File> getS3Files() {
        return s3Files;
    }

    public void setS3Files(List<S3File> s3Files) {
        this.s3Files = s3Files;
    }

}
