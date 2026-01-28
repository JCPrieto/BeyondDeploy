package es.jklabs.gui.navegacion;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import es.jklabs.gui.MainUI;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.CannonicalId;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.UtilidadesS3;
import org.junit.After;
import org.junit.Test;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Timer;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ExploradorTest {

    private static Configuracion crearConfiguracion() {
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        bucketConfig.setAccesKey("ak");
        bucketConfig.setSecretKey("sk");
        bucketConfig.setRegion(Regions.EU_WEST_1);
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
        UtilidadesS3.clearAmazonS3ForTest();
    }

    @Test
    public void uploadFileListaCompletaActualizaPantalla() throws Exception {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        Configuracion configuracion = crearConfiguracion();
        S3Folder raiz = new S3Folder();
        MainUI padre = crearPadre(configuracion, raiz);
        ExploradorProbe explorador = new ExploradorProbe(padre, raiz);
        explorador.detenerTimer();

        File file1 = Files.createTempFile("explorador-upload-1", ".txt").toFile();
        File file2 = Files.createTempFile("explorador-upload-2", ".txt").toFile();
        Files.writeString(file1.toPath(), "uno", StandardCharsets.UTF_8);
        Files.writeString(file2.toPath(), "dos", StandardCharsets.UTF_8);

        when(s3.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult(), new PutObjectResult());
        when(s3.getObjectAcl(eq("bucket"), any(String.class))).thenReturn(new AccessControlList());

        explorador.uploadFile(List.of(file1, file2));

        esperarHasta(() -> explorador.recargarPantallaLlamado);
        assertTrue(explorador.recargarPantallaLlamado);
        assertTrue(explorador.isEnabled());
        verify(s3, times(2)).putObject(any(PutObjectRequest.class));
        verify(s3, times(2)).setObjectAcl(eq("bucket"), any(String.class), any(AccessControlList.class));
    }

    @Test
    public void uploadFileListaParcialActualizaPantalla() throws Exception {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        Configuracion configuracion = crearConfiguracion();
        S3Folder raiz = new S3Folder();
        MainUI padre = crearPadre(configuracion, raiz);
        ExploradorProbe explorador = new ExploradorProbe(padre, raiz);
        explorador.detenerTimer();

        File file1 = Files.createTempFile("explorador-upload-3", ".txt").toFile();
        File file2 = Files.createTempFile("explorador-upload-4", ".txt").toFile();
        Files.writeString(file1.toPath(), "uno", StandardCharsets.UTF_8);
        Files.writeString(file2.toPath(), "dos", StandardCharsets.UTF_8);

        when(s3.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult(), null);
        when(s3.getObjectAcl(eq("bucket"), any(String.class))).thenReturn(new AccessControlList());

        explorador.uploadFile(List.of(file1, file2));

        esperarHasta(() -> explorador.recargarPantallaLlamado);
        assertTrue(explorador.recargarPantallaLlamado);
        assertTrue(explorador.isEnabled());
        verify(s3, times(2)).putObject(any(PutObjectRequest.class));
        verify(s3, times(1)).setObjectAcl(eq("bucket"), any(String.class), any(AccessControlList.class));
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
