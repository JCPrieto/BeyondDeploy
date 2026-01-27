package es.jklabs.utilidades;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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
import org.apache.commons.lang3.Strings;

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

public class UtilidadesS3 {

    private static AmazonS3 amazonS3Override;

    private UtilidadesS3() {

    }

    private static ObjectListing getRaiz(BucketConfig bucketConfig) {
        AmazonS3 s3 = getAmazonS3(bucketConfig);
        return listarTodosObjetos(s3, bucketConfig.getBucketName(), null);
    }

    private static AmazonS3 getAmazonS3(BucketConfig bucketConfig) {
        if (amazonS3Override != null) {
            return amazonS3Override;
        }
        String secretKey;
        try {
            secretKey = UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey());
        } catch (IllegalStateException e) {
            Growls.mostrarError("secret.key.descifrado", e);
            throw new AmazonClientException("Error al descifrar Secret Key", e);
        }
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(bucketConfig.getAccesKey(), secretKey);
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
            try {
                File directorio = fc.getSelectedFile();
                AmazonS3 s3 = getAmazonS3(bucketConfig);
                S3Object s3Object = s3.getObject(bucketConfig.getBucketName(), file.getFullPath());
                download(directorio, s3Object, file.getName());
            } catch (AmazonClientException e) {
                Growls.mostrarError("descargar.archivo", wrapAmazonException(e, "Descargar archivo"));
            } finally {
                ventana.setCursor(null);
            }
        }
    }

    public static boolean uploadFile(File file, String fullpath, Configuracion configuracion) {
        AmazonS3 s3 = getAmazonS3(configuracion.getBucketConfig());
        try {
            PutObjectRequest request = new PutObjectRequest(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), file);
            if (s3.putObject(request) != null) {
                AccessControlList acl = s3.getObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName());
                configuracion.getCannonicalIds().forEach(c ->
                        acl.grantPermission(new CanonicalGrantee(c.getId()), Permission.Read));
                s3.setObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), acl);
                return true;
            }
            return false;
        } catch (AmazonClientException e) {
            Logger.error("subir.archivo", wrapAmazonException(e, "Subir archivo"));
            return false;
        }
    }

    public static ObjectListing getObjetos(BucketConfig bucketConfig, String fullpath) {
        try {
            if (fullpath.isEmpty()) {
                return getRaiz(bucketConfig);
            } else {
                AmazonS3 s3 = getAmazonS3(bucketConfig);
                return listarTodosObjetos(s3, bucketConfig.getBucketName(), fullpath);
            }
        } catch (AmazonClientException e) {
            Growls.mostrarError("cargar.archivos.bucket", wrapAmazonException(e, "Listar objetos"));
            return new ObjectListing();
        }
    }

    public static void actualizarCarpeta(S3Folder folder, ObjectListing elementos) {
        for (S3ObjectSummary s3ObjectSummary : elementos.getObjectSummaries()) {
            String rutaObjeto = Strings.CS.remove(s3ObjectSummary.getKey(), folder.getFullpath());
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
        try {
            AmazonS3 s3 = getAmazonS3(bucketConfig);
            s3.deleteObject(new DeleteObjectRequest(bucketConfig.getBucketName(), s3File.getFullPath()));
            Growls.mostrarInfo("archivo.eliminado.correctamente");
        } catch (AmazonClientException e) {
            Growls.mostrarError("eliminar.archivo", wrapAmazonException(e, "Eliminar archivo"));
        }
    }

    public static void elimninarVersion(BucketConfig bucketConfig, S3File s3File, S3FileVersion s3FileVersion) {
        try {
            AmazonS3 s3 = getAmazonS3(bucketConfig);
            s3.deleteVersion(new DeleteVersionRequest(bucketConfig.getBucketName(), s3File.getFullPath(), s3FileVersion.getId()));
            Growls.mostrarInfo("version.eliminada.correctamente");
        } catch (AmazonClientException e) {
            Growls.mostrarError("eliminar.version", wrapAmazonException(e, "Eliminar version"));
        }
    }

    public static void getObject(MainUI ventana, BucketConfig bucketConfig, S3FileVersion fileVersion) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retorno = fc.showSaveDialog(ventana);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                File directorio = fc.getSelectedFile();
                AmazonS3 s3 = getAmazonS3(bucketConfig);
                S3Object s3Object = s3.getObject(new GetObjectRequest(bucketConfig.getBucketName(), fileVersion.getS3File().getFullPath(), fileVersion.getId()));
                download(directorio, s3Object, getDownloadName(fileVersion));
            } catch (AmazonClientException e) {
                Growls.mostrarError("descargar.archivo", wrapAmazonException(e, "Descargar archivo"));
            } finally {
                ventana.setCursor(null);
            }
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

    private static void download(File directorio, S3Object s3Object, String nombre) {
        byte[] buf = new byte[1024];
        try (InputStream in = s3Object.getObjectContent();
             OutputStream out = Files.newOutputStream(new File(directorio.getAbsolutePath() +
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
        }
    }

    public static List<File> uploadFile(List<File> files, String fullpath, Configuracion configuracion) {
        AmazonS3 s3 = getAmazonS3(configuracion.getBucketConfig());
        List<File> errors = new ArrayList<>();
        for (File file : files) {
            try {
                PutObjectRequest request = new PutObjectRequest(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), file);
                if (s3.putObject(request) != null) {
                    AccessControlList acl = s3.getObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName());
                    configuracion.getCannonicalIds().forEach(c ->
                            acl.grantPermission(new CanonicalGrantee(c.getId()), Permission.Read));
                    s3.setObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), acl);
                } else {
                    errors.add(file);
                }
            } catch (AmazonClientException e) {
                errors.add(file);
                Logger.error("subir.archivo", wrapAmazonException(e, "Subir archivo"));
            }
        }
        return errors;
    }

    private static Exception wrapAmazonException(Exception e, String contexto) {
        if (e instanceof AmazonServiceException ase) {
            String detalle = contexto + " (status=" + ase.getStatusCode() + ", code=" + ase.getErrorCode()
                    + ", requestId=" + ase.getRequestId() + ")";
            return new IOException(detalle, e);
        }
        if (e instanceof AmazonClientException) {
            String detalle = contexto + " (cliente AWS)";
            return new IOException(detalle, e);
        }
        return e;
    }

    private static ObjectListing listarTodosObjetos(AmazonS3 s3, String bucket, String prefix) {
        ObjectListing listing = prefix == null
                ? s3.listObjects(bucket)
                : s3.listObjects(bucket, prefix);
        ObjectListing acumulado = listing;
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            if (listing.getObjectSummaries() != null) {
                acumulado.getObjectSummaries().addAll(listing.getObjectSummaries());
            }
        }
        return acumulado;
    }

    public static PaginadorVersiones crearPaginadorVersiones(BucketConfig bucketConfig, S3File s3File, int maxKeys) {
        return new PaginadorVersiones(getAmazonS3(bucketConfig), bucketConfig.getBucketName(), s3File, maxKeys);
    }

    private static List<S3FileVersion> convertirVersiones(VersionListing listing, S3File s3File) {
        List<S3FileVersion> page = new ArrayList<>();
        if (listing == null) {
            return page;
        }
        for (S3VersionSummary versionSummary : listing.getVersionSummaries()) {
            S3FileVersion s3FileVersion = new S3FileVersion();
            s3FileVersion.setId(versionSummary.getVersionId());
            s3FileVersion.setFecha(versionSummary.getLastModified());
            s3FileVersion.setS3File(s3File);
            page.add(s3FileVersion);
        }
        return page;
    }

    static void setAmazonS3ForTest(AmazonS3 amazonS3) {
        amazonS3Override = amazonS3;
    }

    static void clearAmazonS3ForTest() {
        amazonS3Override = null;
    }

    public static class PaginadorVersiones {
        private final AmazonS3 s3;
        private final String bucket;
        private final S3File s3File;
        private final int maxKeys;
        private VersionListing listing;
        private boolean started;
        private boolean finished;

        private PaginadorVersiones(AmazonS3 s3, String bucket, S3File s3File, int maxKeys) {
            this.s3 = s3;
            this.bucket = bucket;
            this.s3File = s3File;
            this.maxKeys = maxKeys;
        }

        public synchronized List<S3FileVersion> nextPage() {
            if (finished) {
                return new ArrayList<>();
            }
            try {
                if (!started) {
                    listing = s3.listVersions(new ListVersionsRequest(bucket, s3File.getFullPath(), null, null, null, maxKeys));
                    started = true;
                } else if (listing != null && listing.isTruncated()) {
                    listing = s3.listNextBatchOfVersions(listing);
                } else {
                    finished = true;
                    return new ArrayList<>();
                }
                List<S3FileVersion> page = convertirVersiones(listing, s3File);
                if (listing == null || !listing.isTruncated()) {
                    finished = true;
                }
                return page;
            } catch (AmazonClientException e) {
                Growls.mostrarError("cargar.versiones", wrapAmazonException(e, "Listar versiones"));
                finished = true;
                return new ArrayList<>();
            }
        }

        public synchronized boolean hasMore() {
            return !finished;
        }
    }
}
