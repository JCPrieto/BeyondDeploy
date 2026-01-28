package es.jklabs.utilidades;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
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

    private static final long TRANSFER_MANAGER_MIN_SIZE = 16L * 1024 * 1024;
    private static final int PROGRESS_STEP_PERCENT = 10;
    private static AmazonS3 amazonS3Override;
    private static AmazonS3 amazonS3Cache;
    private static String amazonS3CacheKey;
    private static TransferManager transferManagerCache;
    private static String transferManagerCacheKey;
    private static FileChooserProvider fileChooserProvider = new DefaultFileChooserProvider();
    private static ProgressHandler progressHandler = new DefaultProgressHandler();

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
        String cacheKey = getCacheKey(bucketConfig);
        if (amazonS3Cache != null && cacheKey.equals(amazonS3CacheKey)) {
            return amazonS3Cache;
        }
        String secretKey;
        try {
            secretKey = UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey());
        } catch (IllegalStateException e) {
            Growls.mostrarError("secret.key.descifrado", e);
            throw new AmazonClientException("Error al descifrar Secret Key", e);
        }
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(bucketConfig.getAccesKey(), secretKey);
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(bucketConfig.getRegion())
                .build();
        amazonS3Cache = s3;
        amazonS3CacheKey = cacheKey;
        return s3;
    }

    public static void getObject(MainUI ventana, BucketConfig bucketConfig, S3File file) {
        JFileChooser fc = fileChooserProvider.createDirectoryChooser();
        int retorno = fileChooserProvider.showSaveDialog(fc, ventana);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File directorio = fc.getSelectedFile();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        AmazonS3 s3 = getAmazonS3(bucketConfig);
                        long size = getObjectSize(s3, bucketConfig.getBucketName(), file.getFullPath());
                        if (isLargeFile(size)) {
                            downloadWithTransferManager(bucketConfig, new GetObjectRequest(bucketConfig.getBucketName(), file.getFullPath()),
                                    new File(directorio, file.getName()), "Descargar archivo");
                        } else {
                            S3Object s3Object = s3.getObject(bucketConfig.getBucketName(), file.getFullPath());
                            download(directorio, s3Object, file.getName());
                        }
                    } catch (AmazonClientException e) {
                        Growls.mostrarError("descargar.archivo", wrapAmazonException(e, "Descargar archivo"));
                    }
                    return null;
                }

                @Override
                protected void done() {
                    ventana.setCursor(null);
                }
            }.execute();
        }
    }

    public static boolean uploadFile(File file, String fullpath, Configuracion configuracion) {
        AmazonS3 s3 = getAmazonS3(configuracion.getBucketConfig());
        try {
            PutObjectRequest request = new PutObjectRequest(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), file);
            if (isLargeFile(file.length())) {
                Upload upload = getTransferManager(configuracion.getBucketConfig()).upload(request);
                esperarTransfer(upload, "Subida", file.getName());
                if (upload.getState() == com.amazonaws.services.s3.transfer.Transfer.TransferState.Completed) {
                    aplicarPermisosLectura(s3, configuracion, fullpath, file.getName());
                    return true;
                }
                return false;
            }
            progressHandler.onStart("Subida", file.getName());
            if (s3.putObject(request) != null) {
                AccessControlList acl = s3.getObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName());
                configuracion.getCannonicalIds().forEach(c ->
                        acl.grantPermission(new CanonicalGrantee(c.getId()), Permission.Read));
                s3.setObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), acl);
                progressHandler.onProgress(100, "Subida", file.getName());
                progressHandler.onFinish(true, "Subida", file.getName());
                return true;
            }
            progressHandler.onFinish(false, "Subida", file.getName());
            return false;
        } catch (AmazonClientException e) {
            progressHandler.onFinish(false, "Subida", file.getName());
            Logger.error("subir.archivo", wrapAmazonException(e, "Subir archivo"));
            return false;
        } catch (InterruptedException e) {
            progressHandler.onFinish(false, "Subida", file.getName());
            Logger.error("subir.archivo", e);
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
        JFileChooser fc = fileChooserProvider.createDirectoryChooser();
        int retorno = fileChooserProvider.showSaveDialog(fc, ventana);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File directorio = fc.getSelectedFile();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        AmazonS3 s3 = getAmazonS3(bucketConfig);
                        GetObjectRequest request = new GetObjectRequest(bucketConfig.getBucketName(), fileVersion.getS3File().getFullPath(), fileVersion.getId());
                        long size = getObjectSize(s3, new GetObjectMetadataRequest(bucketConfig.getBucketName(),
                                fileVersion.getS3File().getFullPath(), fileVersion.getId()));
                        if (isLargeFile(size)) {
                            downloadWithTransferManager(bucketConfig, request, new File(directorio, getDownloadName(fileVersion)), "Descargar versión");
                        } else {
                            S3Object s3Object = s3.getObject(request);
                            download(directorio, s3Object, getDownloadName(fileVersion));
                        }
                    } catch (AmazonClientException e) {
                        Growls.mostrarError("descargar.archivo", wrapAmazonException(e, "Descargar archivo"));
                    }
                    return null;
                }

                @Override
                protected void done() {
                    ventana.setCursor(null);
                }
            }.execute();
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
        progressHandler.onStart("Descarga", nombre);
        long total = s3Object.getObjectMetadata() != null ? s3Object.getObjectMetadata().getContentLength() : -1L;
        long leidos = 0L;
        int ultimoPorcentaje = -PROGRESS_STEP_PERCENT;
        try (InputStream in = s3Object.getObjectContent();
             OutputStream out = Files.newOutputStream(new File(directorio.getAbsolutePath() +
                     UtilidadesFichero.SEPARADOR + nombre).toPath())) {
            int count;
            while ((count = in.read(buf)) != -1) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                out.write(buf, 0, count);
                if (total > 0) {
                    leidos += count;
                    int porcentaje = (int) (leidos * 100 / total);
                    if (porcentaje >= ultimoPorcentaje + PROGRESS_STEP_PERCENT) {
                        ultimoPorcentaje = porcentaje;
                        progressHandler.onProgress(porcentaje, "Descarga", nombre);
                    }
                }
            }
            in.close();
            progressHandler.onProgress(100, "Descarga", nombre);
            progressHandler.onFinish(true, "Descarga", nombre);
            Growls.mostrarInfo("archivo.descargado.correctamente");
        } catch (IOException e) {
            Growls.mostrarError("descargar.archivo", e);
            progressHandler.onFinish(false, "Descarga", nombre);
        } catch (InterruptedException e) {
            Growls.mostrarError("descargar.archivo", e);
            progressHandler.onFinish(false, "Descarga", nombre);
            Thread.currentThread().interrupt();
        }
    }

    public static List<File> uploadFile(List<File> files, String fullpath, Configuracion configuracion) {
        AmazonS3 s3 = getAmazonS3(configuracion.getBucketConfig());
        List<File> errors = new ArrayList<>();
        for (File file : files) {
            try {
                PutObjectRequest request = new PutObjectRequest(configuracion.getBucketConfig().getBucketName(), fullpath + file.getName(), file);
                if (isLargeFile(file.length())) {
                    Upload upload = getTransferManager(configuracion.getBucketConfig()).upload(request);
                    esperarTransfer(upload, "Subida", file.getName());
                    if (upload.getState() == com.amazonaws.services.s3.transfer.Transfer.TransferState.Completed) {
                        aplicarPermisosLectura(s3, configuracion, fullpath, file.getName());
                    } else {
                        errors.add(file);
                    }
                } else if (s3.putObject(request) != null) {
                    progressHandler.onStart("Subida", file.getName());
                    aplicarPermisosLectura(s3, configuracion, fullpath, file.getName());
                    progressHandler.onProgress(100, "Subida", file.getName());
                    progressHandler.onFinish(true, "Subida", file.getName());
                } else {
                    errors.add(file);
                    progressHandler.onFinish(false, "Subida", file.getName());
                }
            } catch (AmazonClientException e) {
                errors.add(file);
                progressHandler.onFinish(false, "Subida", file.getName());
                Logger.error("subir.archivo", wrapAmazonException(e, "Subir archivo"));
            } catch (InterruptedException e) {
                errors.add(file);
                progressHandler.onFinish(false, "Subida", file.getName());
                Logger.error("subir.archivo", e);
            }
        }
        return errors;
    }

    private static void aplicarPermisosLectura(AmazonS3 s3, Configuracion configuracion, String fullpath, String nombreArchivo) {
        AccessControlList acl = s3.getObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + nombreArchivo);
        configuracion.getCannonicalIds().forEach(c ->
                acl.grantPermission(new CanonicalGrantee(c.getId()), Permission.Read));
        s3.setObjectAcl(configuracion.getBucketConfig().getBucketName(), fullpath + nombreArchivo, acl);
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

    private static TransferManager getTransferManager(BucketConfig bucketConfig) {
        if (amazonS3Override != null) {
            return TransferManagerBuilder.standard()
                    .withS3Client(amazonS3Override)
                    .withMultipartUploadThreshold(TRANSFER_MANAGER_MIN_SIZE)
                    .build();
        }
        String cacheKey = getCacheKey(bucketConfig);
        if (transferManagerCache != null && cacheKey.equals(transferManagerCacheKey)) {
            return transferManagerCache;
        }
        TransferManager manager = TransferManagerBuilder.standard()
                .withS3Client(getAmazonS3(bucketConfig))
                .withMultipartUploadThreshold(TRANSFER_MANAGER_MIN_SIZE)
                .build();
        transferManagerCache = manager;
        transferManagerCacheKey = cacheKey;
        return manager;
    }

    private static String getCacheKey(BucketConfig bucketConfig) {
        return String.join("|",
                bucketConfig.getBucketName() == null ? "" : bucketConfig.getBucketName(),
                bucketConfig.getAccesKey() == null ? "" : bucketConfig.getAccesKey(),
                bucketConfig.getSecretKey() == null ? "" : bucketConfig.getSecretKey(),
                bucketConfig.getRegion() == null ? "" : bucketConfig.getRegion().getName());
    }

    private static boolean isLargeFile(long size) {
        return size >= TRANSFER_MANAGER_MIN_SIZE;
    }

    private static long getObjectSize(AmazonS3 s3, String bucket, String key) {
        try {
            ObjectMetadata metadata = s3.getObjectMetadata(bucket, key);
            return metadata != null ? metadata.getContentLength() : -1L;
        } catch (AmazonClientException e) {
            Logger.error("descargar.archivo", wrapAmazonException(e, "Consultar metadata"));
            return -1L;
        }
    }

    private static long getObjectSize(AmazonS3 s3, GetObjectMetadataRequest request) {
        try {
            ObjectMetadata metadata = s3.getObjectMetadata(request);
            return metadata != null ? metadata.getContentLength() : -1L;
        } catch (AmazonClientException e) {
            Logger.error("descargar.archivo", wrapAmazonException(e, "Consultar metadata"));
            return -1L;
        }
    }

    private static void downloadWithTransferManager(BucketConfig bucketConfig, GetObjectRequest request, File destino, String contexto) {
        try {
            Download download = getTransferManager(bucketConfig).download(request, destino);
            esperarTransfer(download, "Descarga", destino.getName());
            if (download.getState() == com.amazonaws.services.s3.transfer.Transfer.TransferState.Completed) {
                Growls.mostrarInfo("archivo.descargado.correctamente");
            }
        } catch (InterruptedException e) {
            Growls.mostrarError("descargar.archivo", e);
            progressHandler.onFinish(false, "Descarga", destino.getName());
            Thread.currentThread().interrupt();
        } catch (AmazonClientException e) {
            Growls.mostrarError("descargar.archivo", wrapAmazonException(e, contexto));
            progressHandler.onFinish(false, "Descarga", destino.getName());
        }
    }

    private static void esperarTransfer(Transfer transfer, String accion, String nombre) throws InterruptedException {
        progressHandler.onStart(accion, nombre);
        Logger.info(accion + " iniciada: " + nombre);
        double ultimoPorcentaje = -PROGRESS_STEP_PERCENT;
        while (!transfer.isDone()) {
            double porcentaje = transfer.getProgress().getPercentTransferred();
            if (porcentaje >= ultimoPorcentaje + PROGRESS_STEP_PERCENT) {
                ultimoPorcentaje = porcentaje;
                Logger.info(accion + " " + (int) porcentaje + "%: " + nombre);
                progressHandler.onProgress((int) porcentaje, accion, nombre);
            }
            Thread.sleep(500);
        }
        transfer.waitForCompletion();
        if (transfer.getState() == com.amazonaws.services.s3.transfer.Transfer.TransferState.Completed) {
            Logger.info(accion + " completada: " + nombre);
            progressHandler.onProgress(100, accion, nombre);
            progressHandler.onFinish(true, accion, nombre);
        } else {
            progressHandler.onFinish(false, accion, nombre);
        }
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

    public static void setAmazonS3ForTest(AmazonS3 amazonS3) {
        amazonS3Override = amazonS3;
        amazonS3Cache = null;
        amazonS3CacheKey = null;
        transferManagerCache = null;
        transferManagerCacheKey = null;
    }

    public static void clearAmazonS3ForTest() {
        amazonS3Override = null;
        amazonS3Cache = null;
        amazonS3CacheKey = null;
        transferManagerCache = null;
        transferManagerCacheKey = null;
    }

    static void setFileChooserProviderForTest(FileChooserProvider provider) {
        fileChooserProvider = provider;
    }

    static void clearFileChooserProviderForTest() {
        fileChooserProvider = new DefaultFileChooserProvider();
    }

    public static void setProgressHandler(ProgressHandler handler) {
        progressHandler = handler != null ? handler : new DefaultProgressHandler();
    }

    static void clearProgressHandlerForTest() {
        progressHandler = new DefaultProgressHandler();
    }

    interface FileChooserProvider {
        JFileChooser createDirectoryChooser();

        int showSaveDialog(JFileChooser chooser, Component parent);
    }

    public interface ProgressHandler {
        void onStart(String accion, String nombre);

        void onProgress(int porcentaje, String accion, String nombre);

        void onFinish(boolean ok, String accion, String nombre);
    }

    private static class DefaultFileChooserProvider implements FileChooserProvider {
        @Override
        public JFileChooser createDirectoryChooser() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            return chooser;
        }

        @Override
        public int showSaveDialog(JFileChooser chooser, Component parent) {
            return chooser.showSaveDialog(parent);
        }
    }

    private static class DefaultProgressHandler implements ProgressHandler {
        @Override
        public void onStart(String accion, String nombre) {
        }

        @Override
        public void onProgress(int porcentaje, String accion, String nombre) {
        }

        @Override
        public void onFinish(boolean ok, String accion, String nombre) {
        }
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
