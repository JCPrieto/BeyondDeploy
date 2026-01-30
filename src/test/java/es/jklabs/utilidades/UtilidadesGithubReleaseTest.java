package es.jklabs.utilidades;

import es.jklabs.json.github.GitHubAsset;
import es.jklabs.json.github.GitHubRelease;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;

public class UtilidadesGithubReleaseTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void parseReleaseJsonYNormalizaVersion() throws Exception {
        String json = "{"
                + "\"tag_name\":\"v1.2.3\","
                + "\"zipball_url\":\"https://example.com/zipball\","
                + "\"assets\":[{\"name\":\"BeyondDeploy-1.2.3.zip\",\"browser_download_url\":\"https://example.com/app.zip\"}]"
                + "}";
        GitHubRelease release = UtilidadesGithubRelease.parseReleaseJson(json);

        assertEquals("v1.2.3", release.getTagName());
        assertEquals("BeyondDeploy-1.2.3.zip", release.getAssets().getFirst().getName());
        assertEquals("1.2.3", UtilidadesGithubRelease.obtenerVersion(release));
    }

    @Test
    public void seleccionaAssetEsperado() {
        GitHubAsset asset = new GitHubAsset();
        asset.setName("BeyondDeploy-2.0.0.zip");
        asset.setBrowserDownloadUrl("https://example.com/app.zip");
        GitHubRelease release = new GitHubRelease();
        release.setAssets(Collections.singletonList(asset));

        GitHubAsset seleccionado = UtilidadesGithubRelease.seleccionarAsset(release, "2.0.0");

        assertNotNull(seleccionado);
        assertEquals("BeyondDeploy-2.0.0.zip", seleccionado.getName());
    }

    @Test
    public void comparaVersiones() {
        assertEquals(1, UtilidadesGithubRelease.compararVersiones("1.2.4", "1.2.3"));
        assertEquals(0, UtilidadesGithubRelease.compararVersiones("1.2.3", "1.2.3"));
        assertEquals(-1, UtilidadesGithubRelease.compararVersiones("1.2.2", "1.2.3"));
    }

    @Test
    public void usaZipballCuandoNoHayAsset() {
        GitHubRelease release = new GitHubRelease();
        release.setZipballUrl("https://example.com/zipball");

        String url = UtilidadesGithubRelease.obtenerUrlDescarga(release, null);

        assertEquals("https://example.com/zipball", url);
    }

    @Test
    public void descargaNuevaVersionGuardaArchivo() throws Exception {
        GitHubAsset asset = new GitHubAsset();
        asset.setName("BeyondDeploy-1.0.0.zip");
        asset.setBrowserDownloadUrl("https://example.com/app.zip");
        GitHubRelease release = new GitHubRelease();
        release.setAssets(Collections.singletonList(asset));

        File directorio = temp.newFolder("descargas");
        boolean descargado = UtilidadesGithubRelease.descargaNuevaVersion(directorio,
                () -> release,
                url -> new ByteArrayInputStream("zip-data".getBytes()));

        assertTrue(descargado);
        File esperado = new File(directorio, "BeyondDeploy-1.0.0.zip");
        assertTrue(esperado.exists());
        assertTrue(esperado.length() > 0);
    }

    @Test
    public void descargaNuevaVersionSinUrlONombreDevuelveFalse() throws Exception {
        GitHubRelease release = new GitHubRelease();
        File directorio = temp.newFolder("descargas-vacias");

        boolean descargado = UtilidadesGithubRelease.descargaNuevaVersion(directorio,
                () -> release,
                url -> new ByteArrayInputStream(new byte[0]));

        assertFalse(descargado);
    }
}
