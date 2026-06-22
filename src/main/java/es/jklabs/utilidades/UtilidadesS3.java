package es.jklabs.utilidades;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.s3.model.S3Folder;
import org.apache.commons.lang3.Strings;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;
import software.amazon.awssdk.transfer.s3.progress.TransferProgressSnapshot;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;

public class UtilidadesS3 {

    private static final int PROGRESS_STEP_PERCENT = 10;
    private static S3Client s3ClientOverride;
    private static S3Client s3ClientCache;
    private static String s3ClientCacheKey;
    private static S3AsyncClient s3AsyncClientCache;
    private static String s3AsyncClientCacheKey;
    private static S3TransferManager transferManagerOverride;
    private static S3TransferManager transferManagerCache;
    private static String transferManagerCacheKey;
    private static FileChooserProvider fileChooserProvider = new DefaultFileChooserProvider();
    private static ProgressHandler progressHandler = new DefaultProgressHandler();

    private UtilidadesS3() {

    }

    private static ListObjectsV2Response getRaiz(BucketConfig bucketConfig) {
        S3Client s3 = getS3Client(bucketConfig);
        return listarTodosObjetos(s3, bucketConfig.getBucketName(), null);
    }

    private static S3Client getS3Client(BucketConfig bucketConfig) {
        if (s3ClientOverride != null) {
            return s3ClientOverride;
        }
        String cacheKey = getCacheKey(bucketConfig);
        if (s3ClientCache != null && cacheKey.equals(s3ClientCacheKey)) {
            return s3ClientCache;
        }
        String secretKey;
        try {
            secretKey = UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey());
        } catch (IllegalStateException e) {
            Growls.mostrarError("secret.key.descifrado", e);
            throw SdkClientException.create("Error al descifrar Secret Key", e);
        }
        AwsBasicCredentials awsCreds = crearCredencialesConfiguradasPorUsuario(bucketConfig, secretKey);
        S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(bucketConfig.getRegion())
                .build();
        s3ClientCache = s3;
        s3ClientCacheKey = cacheKey;
        return s3;
    }

    private static S3AsyncClient getS3AsyncClient(BucketConfig bucketConfig) {
        String cacheKey = getCacheKey(bucketConfig);
        if (s3AsyncClientCache != null && cacheKey.equals(s3AsyncClientCacheKey)) {
            return s3AsyncClientCache;
        }
        String secretKey;
        try {
            secretKey = UtilidadesEncryptacion.decrypt(bucketConfig.getSecretKey());
        } catch (IllegalStateException e) {
            Growls.mostrarError("secret.key.descifrado", e);
            throw SdkClientException.create("Error al descifrar Secret Key", e);
        }
        AwsBasicCredentials awsCreds = crearCredencialesConfiguradasPorUsuario(bucketConfig, secretKey);
        S3AsyncClient s3 = S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(bucketConfig.getRegion())
                .build();
        s3AsyncClientCache = s3;
        s3AsyncClientCacheKey = cacheKey;
        return s3;
    }

    private static S3TransferManager getTransferManager(BucketConfig bucketConfig) {
        if (transferManagerOverride != null) {
            return transferManagerOverride;
        }
        String cacheKey = getCacheKey(bucketConfig);
        if (transferManagerCache != null && cacheKey.equals(transferManagerCacheKey)) {
            return transferManagerCache;
        }
        S3TransferManager manager = S3TransferManager.builder()
                .s3Client(getS3AsyncClient(bucketConfig))
                .build();
        transferManagerCache = manager;
        transferManagerCacheKey = cacheKey;
        return manager;
    }

    @SuppressWarnings("java:S6240")
    private static AwsBasicCredentials crearCredencialesConfiguradasPorUsuario(BucketConfig bucketConfig,
                                                                               String secretKey) {
        // Credenciales configuradas por el usuario: la secret key se guarda cifrada y no hay claves hardcodeadas.
        return AwsBasicCredentials.create(bucketConfig.getAccesKey(), secretKey); // NOSONAR
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
                        S3Client s3 = getS3Client(bucketConfig);
                        long size = getObjectSize(s3, bucketConfig.getBucketName(), file.getFullPath());
                        GetObjectRequest request = GetObjectRequest.builder()
                                .bucket(bucketConfig.getBucketName())
                                .key(file.getFullPath())
                                .build();
                        download(directorio, s3.getObject(request), file.getName(), size);
                    } catch (SdkException e) {
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
        S3Client s3 = getS3Client(configuracion.getBucketConfig());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(configuracion.getBucketConfig().getBucketName())
                    .key(fullpath + file.getName())
                    .build();
            progressHandler.onStart("Subida", file.getName());
            if (uploadFile(configuracion.getBucketConfig(), request, file)) {
                aplicarPermisosLectura(s3, configuracion, fullpath, file.getName());
                progressHandler.onProgress(100, "Subida", file.getName());
                progressHandler.onFinish(true, "Subida", file.getName());
                return true;
            }
            progressHandler.onFinish(false, "Subida", file.getName());
            return false;
        } catch (SdkException | CompletionException e) {
            progressHandler.onFinish(false, "Subida", file.getName());
            Logger.error("subir.archivo", wrapAmazonException(e, "Subir archivo"));
            return false;
        }
    }

    private static boolean uploadFile(BucketConfig bucketConfig, PutObjectRequest request, File file) {
        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(request)
                .source(file.toPath())
                .addTransferListener(new ProgressTransferListener("Subida", file.getName()))
                .build();
        FileUpload upload = getTransferManager(bucketConfig).uploadFile(uploadFileRequest);
        CompletedFileUpload completed = upload.completionFuture().join();
        return completed != null && completed.response() != null;
    }

    public static ListObjectsV2Response getObjetos(BucketConfig bucketConfig, String fullpath) {
        try {
            if (fullpath.isEmpty()) {
                return getRaiz(bucketConfig);
            } else {
                S3Client s3 = getS3Client(bucketConfig);
                return listarTodosObjetos(s3, bucketConfig.getBucketName(), fullpath);
            }
        } catch (SdkException e) {
            Growls.mostrarError("cargar.archivos.bucket", wrapAmazonException(e, "Listar objetos"));
            return ListObjectsV2Response.builder().build();
        }
    }

    public static void actualizarCarpeta(S3Folder folder, ListObjectsV2Response elementos) {
        for (S3Object s3ObjectSummary : elementos.contents()) {
            String rutaObjeto = Strings.CS.remove(s3ObjectSummary.key(), folder.getFullpath());
            if (!rutaObjeto.isEmpty()) {
                if (rutaObjeto.endsWith("/")) {
                    String[] ruta = rutaObjeto.split("/");
                    folder.addCarpetas(ruta[0], s3ObjectSummary.key());
                } else {
                    if (!rutaObjeto.contains("/")) {
                        folder.getS3Files().add(new S3File(rutaObjeto, s3ObjectSummary.key()));
                    }
                }
            }
        }
    }

    public static void deleteObject(BucketConfig bucketConfig, S3File s3File) {
        try {
            S3Client s3 = getS3Client(bucketConfig);
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketConfig.getBucketName())
                    .key(s3File.getFullPath())
                    .build());
            Growls.mostrarInfo("archivo.eliminado.correctamente");
        } catch (SdkException e) {
            Growls.mostrarError("eliminar.archivo", wrapAmazonException(e, "Eliminar archivo"));
        }
    }

    public static void elimninarVersion(BucketConfig bucketConfig, S3File s3File, S3FileVersion s3FileVersion) {
        try {
            S3Client s3 = getS3Client(bucketConfig);
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketConfig.getBucketName())
                    .key(s3File.getFullPath())
                    .versionId(s3FileVersion.getId())
                    .build());
            Growls.mostrarInfo("version.eliminada.correctamente");
        } catch (SdkException e) {
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
                        S3Client s3 = getS3Client(bucketConfig);
                        GetObjectRequest request = GetObjectRequest.builder()
                                .bucket(bucketConfig.getBucketName())
                                .key(fileVersion.getS3File().getFullPath())
                                .versionId(fileVersion.getId())
                                .build();
                        long size = getObjectSize(s3, HeadObjectRequest.builder()
                                .bucket(bucketConfig.getBucketName())
                                .key(fileVersion.getS3File().getFullPath())
                                .versionId(fileVersion.getId())
                                .build());
                        download(directorio, s3.getObject(request), getDownloadName(fileVersion), size);
                    } catch (SdkException e) {
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

    private static void download(File directorio, ResponseInputStream<GetObjectResponse> s3Object, String nombre,
                                 long total) {
        byte[] buf = new byte[1024];
        progressHandler.onStart("Descarga", nombre);
        long leidos = 0L;
        int ultimoPorcentaje = -PROGRESS_STEP_PERCENT;
        try (InputStream in = s3Object;
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
        S3Client s3 = getS3Client(configuracion.getBucketConfig());
        List<File> errors = new ArrayList<>();
        for (File file : files) {
            try {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(configuracion.getBucketConfig().getBucketName())
                        .key(fullpath + file.getName())
                        .build();
                progressHandler.onStart("Subida", file.getName());
                if (uploadFile(configuracion.getBucketConfig(), request, file)) {
                    aplicarPermisosLectura(s3, configuracion, fullpath, file.getName());
                    progressHandler.onProgress(100, "Subida", file.getName());
                    progressHandler.onFinish(true, "Subida", file.getName());
                } else {
                    errors.add(file);
                    progressHandler.onFinish(false, "Subida", file.getName());
                }
            } catch (SdkException | CompletionException e) {
                errors.add(file);
                progressHandler.onFinish(false, "Subida", file.getName());
                Logger.error("subir.archivo", wrapAmazonException(e, "Subir archivo"));
            }
        }
        return errors;
    }

    private static void aplicarPermisosLectura(S3Client s3, Configuracion configuracion, String fullpath, String nombreArchivo) {
        String key = fullpath + nombreArchivo;
        GetObjectAclResponse acl = s3.getObjectAcl(GetObjectAclRequest.builder()
                .bucket(configuracion.getBucketConfig().getBucketName())
                .key(key)
                .build());
        List<Grant> grants = new ArrayList<>(acl.grants());
        configuracion.getCannonicalIds().forEach(c -> grants.add(Grant.builder()
                .permission(Permission.READ)
                .grantee(Grantee.builder()
                        .id(c.getId())
                        .type(Type.CANONICAL_USER)
                        .build())
                .build()));
        s3.putObjectAcl(PutObjectAclRequest.builder()
                .bucket(configuracion.getBucketConfig().getBucketName())
                .key(key)
                .accessControlPolicy(AccessControlPolicy.builder()
                        .owner(acl.owner())
                        .grants(grants)
                        .build())
                .build());
    }

    private static Exception wrapAmazonException(Exception e, String contexto) {
        if (e instanceof CompletionException ce && ce.getCause() instanceof Exception cause) {
            return wrapAmazonException(cause, contexto);
        }
        if (e instanceof AwsServiceException ase) {
            String detalle = contexto + " (status=" + ase.statusCode() + ", code=" +
                    ase.awsErrorDetails().errorCode() + ", requestId=" + ase.requestId() + ")";
            return new IOException(detalle, e);
        }
        if (e instanceof SdkClientException) {
            String detalle = contexto + " (cliente AWS)";
            return new IOException(detalle, e);
        }
        return e;
    }

    private static ListObjectsV2Response listarTodosObjetos(S3Client s3, String bucket, String prefix) {
        List<S3Object> objetos = new ArrayList<>();
        String continuationToken = null;
        ListObjectsV2Response listing;
        do {
            ListObjectsV2Request.Builder request = ListObjectsV2Request.builder()
                    .bucket(bucket);
            if (continuationToken != null) {
                request.continuationToken(continuationToken);
            }
            if (prefix != null) {
                request.prefix(prefix);
            }
            listing = s3.listObjectsV2(request.build());
            objetos.addAll(listing.contents());
            continuationToken = listing.nextContinuationToken();
        } while (Boolean.TRUE.equals(listing.isTruncated()));
        return ListObjectsV2Response.builder()
                .contents(objetos)
                .isTruncated(false)
                .build();
    }

    private static String getCacheKey(BucketConfig bucketConfig) {
        return String.join("|",
                bucketConfig.getBucketName() == null ? "" : bucketConfig.getBucketName(),
                bucketConfig.getAccesKey() == null ? "" : bucketConfig.getAccesKey(),
                bucketConfig.getSecretKey() == null ? "" : bucketConfig.getSecretKey(),
                bucketConfig.getRegion() == null ? "" : bucketConfig.getRegion().id());
    }

    private static long getObjectSize(S3Client s3, String bucket, String key) {
        try {
            HeadObjectResponse metadata = s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return metadata != null ? metadata.contentLength() : -1L;
        } catch (SdkException e) {
            Logger.error("descargar.archivo", wrapAmazonException(e, "Consultar metadata"));
            return -1L;
        }
    }

    private static long getObjectSize(S3Client s3, HeadObjectRequest request) {
        try {
            HeadObjectResponse metadata = s3.headObject(request);
            return metadata != null ? metadata.contentLength() : -1L;
        } catch (SdkException e) {
            Logger.error("descargar.archivo", wrapAmazonException(e, "Consultar metadata"));
            return -1L;
        }
    }

    public static PaginadorVersiones crearPaginadorVersiones(BucketConfig bucketConfig, S3File s3File, int maxKeys) {
        return new PaginadorVersiones(getS3Client(bucketConfig), bucketConfig.getBucketName(), s3File, maxKeys);
    }

    private static List<S3FileVersion> convertirVersiones(ListObjectVersionsResponse listing, S3File s3File) {
        List<S3FileVersion> page = new ArrayList<>();
        if (listing == null) {
            return page;
        }
        for (ObjectVersion versionSummary : listing.versions()) {
            S3FileVersion s3FileVersion = new S3FileVersion();
            s3FileVersion.setId(versionSummary.versionId());
            if (versionSummary.lastModified() != null) {
                s3FileVersion.setFecha(Date.from(versionSummary.lastModified()));
            }
            s3FileVersion.setS3File(s3File);
            page.add(s3FileVersion);
        }
        return page;
    }

    public static void setS3ClientForTest(S3Client s3Client) {
        s3ClientOverride = s3Client;
        s3ClientCache = null;
        s3ClientCacheKey = null;
        transferManagerCache = null;
        transferManagerCacheKey = null;
    }

    public static void clearS3ClientForTest() {
        s3ClientOverride = null;
        s3ClientCache = null;
        s3ClientCacheKey = null;
        s3AsyncClientCache = null;
        s3AsyncClientCacheKey = null;
        transferManagerOverride = null;
        transferManagerCache = null;
        transferManagerCacheKey = null;
    }

    public static void setTransferManagerForTest(S3TransferManager transferManager) {
        transferManagerOverride = transferManager;
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

    private static class ProgressTransferListener implements TransferListener {
        private final String accion;
        private final String nombre;
        private int ultimoPorcentaje = -PROGRESS_STEP_PERCENT;

        private ProgressTransferListener(String accion, String nombre) {
            this.accion = accion;
            this.nombre = nombre;
        }

        @Override
        public void bytesTransferred(Context.BytesTransferred context) {
            TransferProgressSnapshot snapshot = context.progressSnapshot();
            if (snapshot.totalBytes().isPresent()) {
                int porcentaje = (int) (snapshot.transferredBytes() * 100 / snapshot.totalBytes().getAsLong());
                if (porcentaje >= ultimoPorcentaje + PROGRESS_STEP_PERCENT || porcentaje == 100) {
                    ultimoPorcentaje = porcentaje;
                    progressHandler.onProgress(porcentaje, accion, nombre);
                }
            } else if (snapshot.ratioTransferred().isPresent()) {
                int porcentaje = (int) (snapshot.ratioTransferred().getAsDouble() * 100);
                if (porcentaje >= ultimoPorcentaje + PROGRESS_STEP_PERCENT || porcentaje == 100) {
                    ultimoPorcentaje = porcentaje;
                    progressHandler.onProgress(porcentaje, accion, nombre);
                }
            }
        }
    }

    public static class PaginadorVersiones {
        private final S3Client s3;
        private final String bucket;
        private final S3File s3File;
        private final int maxKeys;
        private ListObjectVersionsResponse listing;
        private boolean started;
        private boolean finished;

        private PaginadorVersiones(S3Client s3, String bucket, S3File s3File, int maxKeys) {
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
                    listing = s3.listObjectVersions(ListObjectVersionsRequest.builder()
                            .bucket(bucket)
                            .prefix(s3File.getFullPath())
                            .maxKeys(maxKeys)
                            .build());
                    started = true;
                } else if (listing != null && listing.isTruncated()) {
                    listing = s3.listObjectVersions(ListObjectVersionsRequest.builder()
                            .bucket(bucket)
                            .prefix(s3File.getFullPath())
                            .keyMarker(listing.nextKeyMarker())
                            .versionIdMarker(listing.nextVersionIdMarker())
                            .maxKeys(maxKeys)
                            .build());
                } else {
                    finished = true;
                    return new ArrayList<>();
                }
                List<S3FileVersion> page = convertirVersiones(listing, s3File);
                if (listing == null || !listing.isTruncated()) {
                    finished = true;
                }
                return page;
            } catch (SdkException e) {
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
