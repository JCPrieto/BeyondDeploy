package es.jklabs.utilidades;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3Folder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Objects;

public class UtilidadesS3 {

    private UtilidadesS3() {

    }

    private static ObjectListing getRaiz(BucketConfig bucketConfig) {
        AmazonS3 s3 = getAmazonS3(bucketConfig);
        return s3.listObjects(bucketConfig.getBucketName());
    }

    private static AmazonS3 getAmazonS3(BucketConfig bucketConfig) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(bucketConfig.getAccesKey(),
                Objects.requireNonNull(UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey())));
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
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File directorio = fc.getSelectedFile();
            AmazonS3 s3 = getAmazonS3(bucketConfig);
            S3Object s3Object = s3.getObject(bucketConfig.getBucketName(), file.getFullPath());
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
                Growls.mostrarInfo("archivo.descargado.correctamente");
            } catch (IOException e) {
                Growls.mostrarError("descargar.archivo", e);
            } catch (InterruptedException e) {
                Growls.mostrarError("descargar.archivo", e);
                Thread.currentThread().interrupt();
            } finally {
                ventana.setCursor(null);
            }
        }
    }

    public static boolean uploadFile(File file, String fullpath, Configuracion configuracion) {
        AmazonS3 s3 = getAmazonS3(configuracion.getBucketConfig());
        PutObjectRequest request = new PutObjectRequest(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), file);
        if (s3.putObject(request) != null) {
            AccessControlList acl = s3.getObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName());
            configuracion.getCannonicalIds().forEach(c ->
                    acl.grantPermission(new CanonicalGrantee(c.getId()), Permission.Read));
            s3.setObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), acl);
            return true;
        }
        return false;
    }

    public static ObjectListing getObjetos(BucketConfig bucketConfig, String fullpath) {
        if (fullpath.isEmpty()) {
            return getRaiz(bucketConfig);
        } else {
            AmazonS3 s3 = getAmazonS3(bucketConfig);
            return s3.listObjects(bucketConfig.getBucketName(), fullpath);
        }
    }

    public static void actualizarCarpeta(S3Folder folder, ObjectListing elementos) {
        for (S3ObjectSummary s3ObjectSummary : elementos.getObjectSummaries()) {
            String rutaObjeto = StringUtils.remove(s3ObjectSummary.getKey(), folder.getFullpath());
            if (!rutaObjeto.isEmpty()) {
                if (rutaObjeto.endsWith("/")) {
                    String[] ruta = rutaObjeto.split("/");
                    folder.addCarpetas(ruta[0], s3ObjectSummary.getKey());
                } else {
                    if (!rutaObjeto.contains("/")) {
                        folder.getS3Files().add(new S3File(rutaObjeto, s3ObjectSummary.getKey()));
                    }
                }
            }
        }
    }

    public static void deleteObject(BucketConfig bucketConfig, S3File s3File) {
        AmazonS3 s3 = getAmazonS3(bucketConfig);
        s3.deleteObject(new DeleteObjectRequest(bucketConfig.getBucketName(), s3File.getFullPath()));
        Growls.mostrarInfo("archivo.eliminado.correctamente");
    }
}
