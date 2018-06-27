package es.jklabs.s3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class S3Folder implements Serializable {

    private static final long serialVersionUID = -4932960673157161234L;
    private List<S3Folder> s3Forlders;
    private String name;
    private List<S3Object> s3Objects;

    public S3Folder() {
        this.s3Forlders = new ArrayList<>();
        this.s3Objects = new ArrayList<>();
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

    public List<S3Object> getS3Objects() {
        return s3Objects;
    }

    public void setS3Objects(List<S3Object> s3Objects) {
        this.s3Objects = s3Objects;
    }

}
