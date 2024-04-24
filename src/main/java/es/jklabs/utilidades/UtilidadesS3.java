package es.jklabs.utilidades;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.s3.model.S3Folder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
                .withRegion(bucketConfig.getRegion())
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
            download(ventana, directorio, s3Object, file.getName());
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

    public static List<S3FileVersion> getVersiones(BucketConfig bucketConfig, S3File s3File) {
        AmazonS3 s3 = getAmazonS3(bucketConfig);
        List<S3VersionSummary> versiones = s3.listVersions(new ListVersionsRequest(bucketConfig.getBucketName(), s3File.getFullPath(), null, null, null, 10)).getVersionSummaries();
        List<S3FileVersion> s3FileVersions = new ArrayList<>();
        for (S3VersionSummary versionSummary : versiones) {
            S3FileVersion s3FileVersion = new S3FileVersion();
            s3FileVersion.setId(versionSummary.getVersionId());
            s3FileVersion.setFecha(versionSummary.getLastModified());
            s3FileVersion.setS3File(s3File);
            s3FileVersions.add(s3FileVersion);
        }
        return s3FileVersions;
    }

    public static void elimninarVersion(BucketConfig bucketConfig, S3File s3File, S3FileVersion s3FileVersion) {
        AmazonS3 s3 = getAmazonS3(bucketConfig);
        s3.deleteVersion(new DeleteVersionRequest(bucketConfig.getBucketName(), s3File.getFullPath(), s3FileVersion.getId()));
        Growls.mostrarInfo("version.eliminada.correctamente");
    }

    public static void getObject(MainUI ventana, BucketConfig bucketConfig, S3FileVersion fileVersion) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retorno = fc.showSaveDialog(ventana);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File directorio = fc.getSelectedFile();
            AmazonS3 s3 = getAmazonS3(bucketConfig);
            S3Object s3Object = s3.getObject(new GetObjectRequest(bucketConfig.getBucketName(), fileVersion.getS3File().getFullPath(), fileVersion.getId()));
            download(ventana, directorio, s3Object, getDownloadName(fileVersion));
        }
    }

    private static String getDownloadName(S3FileVersion fileVersion) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fileVersion.getFecha());
        return String.valueOf(calendar.get(Calendar.YEAR)) +
                calendar.get(Calendar.MONTH) +
                calendar.get(Calendar.DAY_OF_MONTH) +
                calendar.get(Calendar.HOUR_OF_DAY) +
                calendar.get(Calendar.MINUTE) +
                calendar.get(Calendar.SECOND) +
                "_" +
                fileVersion.getS3File().getName();
    }

    private static void download(MainUI ventana, File directorio, S3Object s3Object, String nombre) {
        InputStream in = s3Object.getObjectContent();
        byte[] buf = new byte[1024];
        try (OutputStream out = Files.newOutputStream(new File(directorio.getAbsolutePath() +
                UtilidadesFichero.SEPARADOR + nombre).toPath())) {
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

    public static List<File> uploadFile(List<File> files, String fullpath, Configuracion configuracion) {
        AmazonS3 s3 = getAmazonS3(configuracion.getBucketConfig());
        List<File> errors = new ArrayList<>();
        for (File file : files) {
            PutObjectRequest request = new PutObjectRequest(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), file);
            if (s3.putObject(request) != null) {
                AccessControlList acl = s3.getObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName());
                configuracion.getCannonicalIds().forEach(c ->
                        acl.grantPermission(new CanonicalGrantee(c.getId()), Permission.Read));
                s3.setObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), acl);
            } else {
                errors.add(file);
            }
        }
        return errors;
    }
}
