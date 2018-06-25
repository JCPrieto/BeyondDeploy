package es.jklabs.utilidades;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import es.jklabs.json.configuracion.BucketConfig;

public class UtilidadesS3 {

    private UtilidadesS3() {

    }

    public static ObjectListing getRaiz(BucketConfig bucketConfig) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(bucketConfig.getAccesKey(),
                UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey()));
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.EU_WEST_3)
                .build();
        return s3.listObjects(bucketConfig.getBucketName());
    }
}
