package es.jklabs.gui.navegacion;

import es.jklabs.gui.MainUI;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.CannonicalId;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.UtilidadesS3;
import org.junit.After;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExploradorTest {

    private static Configuracion crearConfiguracion() {
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        bucketConfig.setAccesKey("ak");
        bucketConfig.setSecretKey("sk");
        bucketConfig.setRegion(Region.EU_WEST_1);
        Configuracion configuracion = new Configuracion();
        configuracion.setBucketConfig(bucketConfig);
        configuracion.setCannonicalIds(List.of(new CannonicalId("demo", "id-1")));
        return configuracion;
    }

    private static MainUI crearPadre(Configuracion configuracion, S3Folder raiz) {
        MainUI padre = mock(MainUI.class);
        when(padre.getConfiguracion()).thenReturn(configuracion);
        when(padre.getRaiz()).thenReturn(raiz);
        when(padre.getPanelCentral()).thenReturn(null);
        return padre;
    }

    private static S3TransferManager configurarTransferManager(PutObjectResponse... responses) {
        S3TransferManager transferManager = mock(S3TransferManager.class);
        FileUpload[] uploads = Arrays.stream(responses)
                .map(ExploradorTest::crearFileUpload)
                .toArray(FileUpload[]::new);
        when(transferManager.uploadFile(any(UploadFileRequest.class))).thenReturn(uploads[0],
                Arrays.copyOfRange(uploads, 1, uploads.length));
        UtilidadesS3.setTransferManagerForTest(transferManager);
        return transferManager;
    }

    private static FileUpload crearFileUpload(PutObjectResponse response) {
        FileUpload upload = mock(FileUpload.class);
        CompletedFileUpload completed = response == null ? null : CompletedFileUpload.builder()
                .response(response)
                .build();
        when(upload.completionFuture()).thenReturn(CompletableFuture.completedFuture(completed));
        return upload;
    }

    private static void esperarHasta(Condicion condicion) throws Exception {
        int intentos = 0;
        while (intentos < 100 && !condicion.cumple()) {
            Thread.sleep(50);
            try {
                SwingUtilities.invokeAndWait(() -> {
                    // fuerza el EDT a procesar los callbacks de SwingWorker
                });
            } catch (Exception ignored) {
                // ignore
            }
            intentos++;
        }
    }

    @After
    public void cleanup() {
        UtilidadesS3.clearS3ClientForTest();
    }

    @Test
    public void uploadFileListaCompletaActualizaPantalla() throws Exception {
        S3Client s3 = mock(S3Client.class);
        UtilidadesS3.setS3ClientForTest(s3);
        Configuracion configuracion = crearConfiguracion();
        S3Folder raiz = new S3Folder();
        MainUI padre = crearPadre(configuracion, raiz);
        ExploradorProbe explorador = new ExploradorProbe(padre, raiz);
        explorador.detenerTimer();

        File file1 = Files.createTempFile("explorador-upload-1", ".txt").toFile();
        File file2 = Files.createTempFile("explorador-upload-2", ".txt").toFile();
        Files.writeString(file1.toPath(), "uno", StandardCharsets.UTF_8);
        Files.writeString(file2.toPath(), "dos", StandardCharsets.UTF_8);

        S3TransferManager transferManager = configurarTransferManager(
                PutObjectResponse.builder().build(), PutObjectResponse.builder().build());
        when(s3.getObjectAcl(any(GetObjectAclRequest.class))).thenReturn(GetObjectAclResponse.builder()
                .owner(Owner.builder().id("owner").build())
                .grants(List.<Grant>of())
                .build());

        explorador.uploadFile(List.of(file1, file2));

        esperarHasta(() -> explorador.recargarPantallaLlamado);
        assertTrue(explorador.recargarPantallaLlamado);
        assertTrue(explorador.isEnabled());
        verify(transferManager, times(2)).uploadFile(any(UploadFileRequest.class));
        verify(s3, times(2)).putObjectAcl(any(PutObjectAclRequest.class));
    }

    @Test
    public void uploadFileListaParcialActualizaPantalla() throws Exception {
        S3Client s3 = mock(S3Client.class);
        UtilidadesS3.setS3ClientForTest(s3);
        Configuracion configuracion = crearConfiguracion();
        S3Folder raiz = new S3Folder();
        MainUI padre = crearPadre(configuracion, raiz);
        ExploradorProbe explorador = new ExploradorProbe(padre, raiz);
        explorador.detenerTimer();

        File file1 = Files.createTempFile("explorador-upload-3", ".txt").toFile();
        File file2 = Files.createTempFile("explorador-upload-4", ".txt").toFile();
        Files.writeString(file1.toPath(), "uno", StandardCharsets.UTF_8);
        Files.writeString(file2.toPath(), "dos", StandardCharsets.UTF_8);

        S3TransferManager transferManager = configurarTransferManager(
                PutObjectResponse.builder().build(), null);
        when(s3.getObjectAcl(any(GetObjectAclRequest.class))).thenReturn(GetObjectAclResponse.builder()
                .owner(Owner.builder().id("owner").build())
                .grants(List.<Grant>of())
                .build());

        explorador.uploadFile(List.of(file1, file2));

        esperarHasta(() -> explorador.recargarPantallaLlamado);
        assertTrue(explorador.recargarPantallaLlamado);
        assertTrue(explorador.isEnabled());
        verify(transferManager, times(2)).uploadFile(any(UploadFileRequest.class));
        verify(s3, times(1)).putObjectAcl(any(PutObjectAclRequest.class));
    }

    @FunctionalInterface
    private interface Condicion {
        boolean cumple();
    }

    private static class ExploradorProbe extends Explorador {
        private boolean recargarPantallaLlamado;

        private ExploradorProbe(MainUI padre, S3Folder folder) {
            super(padre, folder);
        }

        @Override
        public void recargarPantalla() {
            recargarPantallaLlamado = true;
        }

        private void detenerTimer() throws Exception {
            Field field = Explorador.class.getDeclaredField("timer");
            field.setAccessible(true);
            Timer timer = (Timer) field.get(this);
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        }
    }
}
