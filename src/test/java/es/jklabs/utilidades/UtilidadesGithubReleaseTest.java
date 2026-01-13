package es.jklabs.utilidades;

import es.jklabs.json.github.GitHubAsset;
import es.jklabs.json.github.GitHubRelease;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UtilidadesGithubReleaseTest {

    @Test
    public void parseReleaseJsonYNormalizaVersion() throws Exception {
        String json = "{"
                + "\"tag_name\":\"v1.2.3\","
                + "\"zipball_url\":\"https://example.com/zipball\","
                + "\"assets\":[{\"name\":\"BeyondDeploy-1.2.3.zip\",\"browser_download_url\":\"https://example.com/app.zip\"}]"
                + "}";
        GitHubRelease release = UtilidadesGithubRelease.parseReleaseJson(json);

        assertEquals("v1.2.3", release.getTagName());
        assertEquals("BeyondDeploy-1.2.3.zip", release.getAssets().get(0).getName());
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
}
