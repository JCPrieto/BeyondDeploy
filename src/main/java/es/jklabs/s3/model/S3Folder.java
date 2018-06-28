package es.jklabs.s3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public S3Folder(String name, String fullpath) {
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

}
