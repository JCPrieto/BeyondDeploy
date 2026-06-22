package es.jklabs.json.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.regions.Region;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

public class BucketConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -6696529132684442401L;
    private String bucketName;
    private String accesKey;
    private String secretKey;
    private String region;

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

    private static String normalizarRegion(String region) {
        if (region == null || region.isBlank()) {
            return region;
        }
        String normalized = region.trim();
        if (normalized.indexOf('_') >= 0) {
            return normalized.toLowerCase(Locale.ROOT).replace('_', '-');
        }
        return normalized;
    }

    @JsonIgnore
    public Region getRegion() {
        return region == null || region.isBlank() ? null : Region.of(region);
    }

    @JsonIgnore
    public void setRegion(Region region) {
        this.region = region == null ? null : region.id();
    }

    @JsonProperty("region")
    public String getRegionId() {
        return region;
    }

    @JsonProperty("region")
    public void setRegionId(String region) {
        this.region = normalizarRegion(region);
    }
}
