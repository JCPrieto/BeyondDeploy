package es.jklabs.json.configuracion;

import java.io.Serializable;
import java.util.List;

public class Configuracion implements Serializable {

    private static final long serialVersionUID = 3896665957540369678L;

    private BucketConfig bucketConfig;
    private List<CannonicalId> cannonicalIds;

    public Configuracion() {
    }

    public BucketConfig getBucketConfig() {
        return bucketConfig;
    }

    public void setBucketConfig(BucketConfig bucketConfig) {
        this.bucketConfig = bucketConfig;
    }

    public List<CannonicalId> getCannonicalIds() {
        return cannonicalIds;
    }

    public void setCannonicalIds(List<CannonicalId> cannonicalIds) {
        this.cannonicalIds = cannonicalIds;
    }
}
