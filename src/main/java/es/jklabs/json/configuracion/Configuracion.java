package es.jklabs.json.configuracion;

import java.io.Serializable;

public class Configuracion implements Serializable {

    private static final long serialVersionUID = 3896665957540369678L;

    private BucketConfig bucketConfig;

    public Configuracion() {
        bucketConfig = new BucketConfig();
    }

    public BucketConfig getBucketConfig() {
        return bucketConfig;
    }

    public void setBucketConfig(BucketConfig bucketConfig) {
        this.bucketConfig = bucketConfig;
    }

}
