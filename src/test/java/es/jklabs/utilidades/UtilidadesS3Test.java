package es.jklabs.utilidades;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.s3.model.S3Folder;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UtilidadesS3Test {

    @After
    public void cleanup() {
        UtilidadesS3.clearAmazonS3ForTest();
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
        assertEquals("v1", primera.get(0).getId());
        assertEquals(1, segunda.size());
        assertEquals("v2", segunda.get(0).getId());
        assertEquals(false, paginador.hasMore());
        verify(s3).listVersions(any());
        verify(s3).listNextBatchOfVersions(page1);
    }
}
