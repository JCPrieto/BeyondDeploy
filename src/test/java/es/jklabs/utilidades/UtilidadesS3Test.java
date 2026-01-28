package es.jklabs.utilidades;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import es.jklabs.gui.MainUI;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.CannonicalId;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.s3.model.S3Folder;
import org.junit.After;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UtilidadesS3Test {

    private static boolean esperarArchivo(File archivo) throws InterruptedException {
        int intentos = 0;
        while (intentos < 50 && !archivo.exists()) {
            Thread.sleep(50);
            intentos++;
        }
        return archivo.exists();
    }

    @Test
    public void actualizarCarpetaAgregaCarpetasYArchivos() {
        S3Folder raiz = new S3Folder();
        ObjectListing listing = new ObjectListing();
        S3ObjectSummary carpeta = new S3ObjectSummary();
        carpeta.setKey("folder1/");
        S3ObjectSummary fichero = new S3ObjectSummary();
        fichero.setKey("file1.txt");
        S3ObjectSummary nested = new S3ObjectSummary();
        nested.setKey("folder1/file2.txt");
        listing.getObjectSummaries().add(carpeta);
        listing.getObjectSummaries().add(fichero);
        listing.getObjectSummaries().add(nested);

        UtilidadesS3.actualizarCarpeta(raiz, listing);

        assertEquals(1, raiz.getS3Forlders().size());
        assertEquals("folder1", raiz.getS3Forlders().getFirst().getName());
        assertEquals(1, raiz.getS3Files().size());
        assertEquals("file1.txt", raiz.getS3Files().getFirst().getName());
    }

    @Test
    public void getDownloadNameConcatenaFechaYSufijo() throws Exception {
        S3FileVersion version = new S3FileVersion();
        version.setS3File(new S3File("file.txt", "file.txt"));
        Calendar cal = new GregorianCalendar(2024, Calendar.JANUARY, 2, 3, 4, 5);
        version.setFecha(cal.getTime());

        Method method = UtilidadesS3.class.getDeclaredMethod("getDownloadName", S3FileVersion.class);
        method.setAccessible(true);
        String nombre = (String) method.invoke(null, version);

        assertEquals("202402345_file.txt", nombre);
    }

    @Test
    public void wrapAmazonExceptionIncluyeStatusYRequestId() throws Exception {
        AmazonServiceException ase = new AmazonServiceException("boom");
        ase.setStatusCode(403);
        ase.setErrorCode("AccessDenied");
        ase.setRequestId("req-1");

        Method method = UtilidadesS3.class.getDeclaredMethod("wrapAmazonException", Exception.class, String.class);
        method.setAccessible(true);
        Exception wrapped = (Exception) method.invoke(null, ase, "Test");

        assertTrue(wrapped instanceof IOException);
        assertTrue(wrapped.getMessage().contains("Test"));
        assertTrue(wrapped.getMessage().contains("status=403"));
        assertTrue(wrapped.getMessage().contains("code=AccessDenied"));
        assertTrue(wrapped.getMessage().contains("requestId=req-1"));
    }

    @Test
    public void wrapAmazonExceptionMarcaClienteAws() throws Exception {
        AmazonClientException ace = new AmazonClientException("boom");

        Method method = UtilidadesS3.class.getDeclaredMethod("wrapAmazonException", Exception.class, String.class);
        method.setAccessible(true);
        Exception wrapped = (Exception) method.invoke(null, ace, "Test");

        assertTrue(wrapped instanceof IOException);
        assertTrue(wrapped.getMessage().contains("cliente AWS"));
    }

    @Test
    public void wrapAmazonExceptionDevuelveOriginalSiNoEsAws() throws Exception {
        IllegalStateException original = new IllegalStateException("boom");

        Method method = UtilidadesS3.class.getDeclaredMethod("wrapAmazonException", Exception.class, String.class);
        method.setAccessible(true);
        Exception wrapped = (Exception) method.invoke(null, original, "Test");

        assertSame(original, wrapped);
    }

    @Test
    public void getObjetosPaginaTodosLosResultados() {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");

        ObjectListing page1 = new ObjectListing();
        page1.setTruncated(true);
        S3ObjectSummary obj1 = new S3ObjectSummary();
        obj1.setKey("prefix/a.txt");
        page1.getObjectSummaries().add(obj1);

        ObjectListing page2 = new ObjectListing();
        page2.setTruncated(false);
        S3ObjectSummary obj2 = new S3ObjectSummary();
        obj2.setKey("prefix/b.txt");
        page2.getObjectSummaries().add(obj2);

        when(s3.listObjects("bucket", "prefix/")).thenReturn(page1);
        when(s3.listNextBatchOfObjects(page1)).thenReturn(page2);

        ObjectListing resultado = UtilidadesS3.getObjetos(bucketConfig, "prefix/");

        assertEquals(2, resultado.getObjectSummaries().size());
        assertEquals("prefix/a.txt", resultado.getObjectSummaries().get(0).getKey());
        assertEquals("prefix/b.txt", resultado.getObjectSummaries().get(1).getKey());
        verify(s3).listNextBatchOfObjects(page1);
    }

    private static File[] esperarArchivoConPrefijo(File directorio, String sufijo) throws InterruptedException {
        int intentos = 0;
        File[] archivos = directorio.listFiles((dir, name) -> name.endsWith(sufijo));
        while (intentos < 50 && (archivos == null || archivos.length == 0)) {
            Thread.sleep(50);
            archivos = directorio.listFiles((dir, name) -> name.endsWith(sufijo));
            intentos++;
        }
        return archivos;
    }

    @After
    public void cleanup() {
        UtilidadesS3.clearAmazonS3ForTest();
        UtilidadesS3.clearFileChooserProviderForTest();
        UtilidadesS3.clearProgressHandlerForTest();
    }

    @Test
    public void crearPaginadorVersionesPaginaBajoDemanda() {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        S3File s3File = new S3File("file.txt", "file.txt");

        VersionListing page1 = new VersionListing();
        page1.setTruncated(true);
        S3VersionSummary v1 = new S3VersionSummary();
        v1.setVersionId("v1");
        page1.getVersionSummaries().add(v1);

        VersionListing page2 = new VersionListing();
        page2.setTruncated(false);
        S3VersionSummary v2 = new S3VersionSummary();
        v2.setVersionId("v2");
        page2.getVersionSummaries().add(v2);

        when(s3.listVersions(any())).thenReturn(page1);
        when(s3.listNextBatchOfVersions(page1)).thenReturn(page2);

        UtilidadesS3.PaginadorVersiones paginador = UtilidadesS3.crearPaginadorVersiones(bucketConfig, s3File, 10);

        List<S3FileVersion> primera = paginador.nextPage();
        List<S3FileVersion> segunda = paginador.nextPage();

        assertEquals(1, primera.size());
        assertEquals("v1", primera.getFirst().getId());
        assertEquals(1, segunda.size());
        assertEquals("v2", segunda.getFirst().getId());
        assertFalse(paginador.hasMore());
        verify(s3).listVersions(any());
        verify(s3).listNextBatchOfVersions(page1);
    }

    @Test
    public void getObjectDescargaArchivoEnDirectorioSeleccionado() throws Exception {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        S3File s3File = new S3File("archivo.txt", "folder/archivo.txt");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(1024);
        when(s3.getObjectMetadata("bucket", "folder/archivo.txt")).thenReturn(metadata);

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream("hola".getBytes(StandardCharsets.UTF_8)));
        when(s3.getObject("bucket", "folder/archivo.txt")).thenReturn(s3Object);

        File directorio = Files.createTempDirectory("s3-download").toFile();
        UtilidadesS3.setFileChooserProviderForTest(new TestFileChooserProvider(directorio));

        UtilidadesS3.getObject(mock(MainUI.class), bucketConfig, s3File);

        File descargado = new File(directorio, "archivo.txt");
        assertTrue(esperarArchivo(descargado));
        assertEquals("hola", Files.readString(descargado.toPath(), StandardCharsets.UTF_8));
        verify(s3).getObject("bucket", "folder/archivo.txt");
    }

    @Test
    public void getObjectVersionDescargaArchivoEnDirectorioSeleccionado() throws Exception {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        S3File s3File = new S3File("archivo.txt", "folder/archivo.txt");
        S3FileVersion version = new S3FileVersion();
        version.setId("v1");
        version.setFecha(new GregorianCalendar(2024, Calendar.JANUARY, 2, 3, 4, 5).getTime());
        version.setS3File(s3File);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(1024);
        when(s3.getObjectMetadata(any(GetObjectMetadataRequest.class))).thenReturn(metadata);

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream("version".getBytes(StandardCharsets.UTF_8)));
        when(s3.getObject(any(com.amazonaws.services.s3.model.GetObjectRequest.class))).thenReturn(s3Object);

        File directorio = Files.createTempDirectory("s3-download-version").toFile();
        UtilidadesS3.setFileChooserProviderForTest(new TestFileChooserProvider(directorio));

        UtilidadesS3.getObject(mock(MainUI.class), bucketConfig, version);

        File[] archivos = esperarArchivoConPrefijo(directorio, "_archivo.txt");
        assertNotNull(archivos);
        assertEquals(1, archivos.length);
        assertEquals("version", Files.readString(archivos[0].toPath(), StandardCharsets.UTF_8));
        verify(s3).getObject(any(com.amazonaws.services.s3.model.GetObjectRequest.class));
    }

    @Test
    public void uploadFileSubeArchivoYAplicaPermisos() throws Exception {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        Configuracion configuracion = new Configuracion();
        configuracion.setBucketConfig(bucketConfig);
        configuracion.setCannonicalIds(List.of(new CannonicalId("demo", "id-1")));

        File file = Files.createTempFile("s3-upload", ".txt").toFile();
        Files.writeString(file.toPath(), "contenido", StandardCharsets.UTF_8);

        when(s3.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());
        when(s3.getObjectAcl(eq("bucket"), eq("prefix/" + file.getName()))).thenReturn(new AccessControlList());

        boolean resultado = UtilidadesS3.uploadFile(file, "prefix/", configuracion);

        assertTrue(resultado);
        verify(s3).putObject(any(PutObjectRequest.class));
        verify(s3).setObjectAcl(eq("bucket"), eq("prefix/" + file.getName()), any(AccessControlList.class));
    }

    @Test
    public void uploadFileListaSubeArchivosYAplicaPermisos() throws Exception {
        AmazonS3 s3 = mock(AmazonS3.class);
        UtilidadesS3.setAmazonS3ForTest(s3);
        BucketConfig bucketConfig = new BucketConfig();
        bucketConfig.setBucketName("bucket");
        Configuracion configuracion = new Configuracion();
        configuracion.setBucketConfig(bucketConfig);
        configuracion.setCannonicalIds(List.of(new CannonicalId("demo", "id-1")));

        File file1 = Files.createTempFile("s3-upload-1", ".txt").toFile();
        File file2 = Files.createTempFile("s3-upload-2", ".txt").toFile();
        Files.writeString(file1.toPath(), "uno", StandardCharsets.UTF_8);
        Files.writeString(file2.toPath(), "dos", StandardCharsets.UTF_8);

        when(s3.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());
        when(s3.getObjectAcl(eq("bucket"), any(String.class))).thenReturn(new AccessControlList());

        List<File> errores = UtilidadesS3.uploadFile(List.of(file1, file2), "prefix/", configuracion);

        assertTrue(errores.isEmpty());
        verify(s3, times(2)).putObject(any(PutObjectRequest.class));
        verify(s3, times(2)).setObjectAcl(eq("bucket"), any(String.class), any(AccessControlList.class));
    }

    @Test
    public void setProgressHandlerAsignaInstanciaPersonalizada() throws Exception {
        UtilidadesS3.ProgressHandler handler = new UtilidadesS3.ProgressHandler() {
            @Override
            public void onStart(String accion, String nombre) {
            }

            @Override
            public void onProgress(int porcentaje, String accion, String nombre) {
            }

            @Override
            public void onFinish(boolean ok, String accion, String nombre) {
            }
        };

        UtilidadesS3.setProgressHandler(handler);

        Field field = UtilidadesS3.class.getDeclaredField("progressHandler");
        field.setAccessible(true);
        Object actual = field.get(null);
        assertSame(handler, actual);
    }

    @Test
    public void setProgressHandlerConNullRestauraDefault() throws Exception {
        UtilidadesS3.setProgressHandler(null);

        Field field = UtilidadesS3.class.getDeclaredField("progressHandler");
        field.setAccessible(true);
        Object actual = field.get(null);
        assertNotNull(actual);
        assertEquals("DefaultProgressHandler", actual.getClass().getSimpleName());
    }

    private static class TestFileChooserProvider implements UtilidadesS3.FileChooserProvider {
        private final File directorio;

        private TestFileChooserProvider(File directorio) {
            this.directorio = directorio;
        }

        @Override
        public JFileChooser createDirectoryChooser() {
            return new JFileChooser() {
                @Override
                public File getSelectedFile() {
                    return directorio;
                }

                @Override
                public void setSelectedFile(File file) {
                    // no-op para evitar efectos colaterales en tests headless
                }
            };
        }

        @Override
        public int showSaveDialog(JFileChooser chooser, Component parent) {
            return JFileChooser.APPROVE_OPTION;
        }
    }
}
