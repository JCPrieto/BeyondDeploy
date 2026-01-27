package es.jklabs.utilidades;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.s3.model.S3Folder;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class UtilidadesS3Test {

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
        assertEquals("folder1", raiz.getS3Forlders().get(0).getName());
        assertEquals(1, raiz.getS3Files().size());
        assertEquals("file1.txt", raiz.getS3Files().get(0).getName());
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
}
