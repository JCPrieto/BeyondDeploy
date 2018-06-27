package es.jklabs.s3.model;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.Serializable;

public class S3Object implements Serializable {

    private final S3ObjectSummary s3ObjectSummary;
    private String name;

    public S3Object(String name, S3ObjectSummary s3ObjectSummary) {
        this.name = name;
        this.s3ObjectSummary = s3ObjectSummary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public S3ObjectSummary getS3ObjectSummary() {
        return s3ObjectSummary;
    }
}
