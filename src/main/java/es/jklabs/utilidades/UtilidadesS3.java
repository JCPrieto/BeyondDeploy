package es.jklabs.utilidades;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.s3.model.S3File;

import javax.swing.*;
import java.io.*;

public class UtilidadesS3 {

    private UtilidadesS3() {

    }

    public static ObjectListing getRaiz(BucketConfig bucketConfig) {
        AmazonS3 s3 = getAmazonS3(bucketConfig);
        return s3.listObjects(bucketConfig.getBucketName());
    }

    private static AmazonS3 getAmazonS3(BucketConfig bucketConfig) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(bucketConfig.getAccesKey(),
                UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey()));
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.EU_WEST_3)
                .build();
    }

    public static void getObject(MainUI ventana, BucketConfig bucketConfig, S3File file) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retorno = fc.showSaveDialog(ventana);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File directorio = fc.getSelectedFile();
            AmazonS3 s3 = getAmazonS3(bucketConfig);
            S3Object s3Object = s3.getObject(bucketConfig.getBucketName(), file.getS3ObjectSummary().getKey());
            InputStream in = s3Object.getObjectContent();
            byte[] buf = new byte[1024];
            try (OutputStream out = new FileOutputStream(new File(directorio.getAbsolutePath() +
                    UtilidadesFichero.SEPARADOR + file.getName()))) {
                int count;
                while ((count = in.read(buf)) != -1) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    out.write(buf, 0, count);
                }
                in.close();
                Growls.mostrarInfo(ventana, null, "archivo.descargado.correctamente");
            } catch (InterruptedException | IOException e) {
                Growls.mostrarError(ventana, null, "descargar.archivo", e);
            }
        }
    }
}
