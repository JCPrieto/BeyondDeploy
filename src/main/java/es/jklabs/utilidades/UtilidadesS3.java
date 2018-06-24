package es.jklabs.utilidades;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;

public class UtilidadesS3 {

    private UtilidadesS3() {

    }

    public static ObjectListing getRaiz() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("REDACTED_AWS_ACCESS_KEY", "REDACTED_AWS_SECRET_KEY");
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.EU_WEST_3)
                .build();
        return s3.listObjects("deploy-bu");
    }
}
