package es.jklabs.json.configuracion;

import com.amazonaws.regions.Regions;

import java.io.Serializable;

public class BucketConfig implements Serializable {

    private static final long serialVersionUID = -6696529132684442401L;
    private String bucketName;
    private String accesKey;
    private String secretKey;
    private Regions region;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccesKey() {
        return accesKey;
    }

    public void setAccesKey(String accesKey) {
        this.accesKey = accesKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Regions getRegion() {
        return region;
    }

    public void setRegion(Regions region) {
        this.region = region;
    }
}
