package es.jklabs.utilidades;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.github.GitHubAsset;
import es.jklabs.json.github.GitHubRelease;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

public class UtilidadesGithubRelease {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_URL = "https://api.github.com/repos/" + Constantes.GITHUB_OWNER + "/"
            + Constantes.GITHUB_REPO + "/releases/latest";

    private UtilidadesGithubRelease() {

    }

    public static boolean existeNuevaVersion() throws IOException {
        GitHubRelease release = obtenerUltimaRelease();
        String version = obtenerVersion(release);
        if (version == null) {
            return false;
        }
        return compararVersiones(version, Constantes.VERSION) > 0;
    }

    public static void descargaNuevaVersion(MainUI ventana) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retorno = fc.showSaveDialog(ventana);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File directorio = fc.getSelectedFile();
            try {
                GitHubRelease release = obtenerUltimaRelease();
                String version = obtenerVersion(release);
                GitHubAsset asset = seleccionarAsset(release, version);
                String urlDescarga = obtenerUrlDescarga(release, asset);
                String nombreArchivo = obtenerNombreArchivo(asset, version);
                if (urlDescarga == null || nombreArchivo == null) {
                    Logger.info("No se pudo resolver la URL de descarga de la release");
                    return;
                }
                Path destino = new File(directorio, nombreArchivo).toPath();
                try (InputStream in = abrirStreamDescarga(urlDescarga)) {
                    Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
                }
                Growls.mostrarInfo("nueva.version.descargada");
            } catch (AccessDeniedException e) {
                Growls.mostrarError("path.sin.permiso.escritura", e);
                descargaNuevaVersion(ventana);
            } catch (IOException e) {
                Logger.error("descargar.nueva.version", e);
            }
        }
    }

    static GitHubRelease parseReleaseJson(String json) throws IOException {
        return MAPPER.readValue(json, GitHubRelease.class);
    }

    static String obtenerVersion(GitHubRelease release) {
        if (release == null) {
            return null;
        }
        String version = release.getTagName();
        if (version == null || version.isEmpty()) {
            version = release.getName();
        }
        return normalizarVersion(version);
    }

    static GitHubAsset seleccionarAsset(GitHubRelease release, String version) {
        if (release == null || release.getAssets() == null || release.getAssets().isEmpty()) {
            return null;
        }
        String esperado = nombreArchivoPorVersion(version);
        List<GitHubAsset> assets = release.getAssets();
        for (GitHubAsset asset : assets) {
            if (asset.getName() != null && asset.getName().equalsIgnoreCase(esperado)) {
                return asset;
            }
        }
        for (GitHubAsset asset : assets) {
            if (asset.getName() != null && asset.getName().toLowerCase(Locale.ROOT).contains(Constantes.NOMBRE_APP.toLowerCase(Locale.ROOT))
                    && asset.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                return asset;
            }
        }
        for (GitHubAsset asset : assets) {
            if (asset.getName() != null && asset.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                return asset;
            }
        }
        return assets.get(0);
    }

    static String obtenerUrlDescarga(GitHubRelease release, GitHubAsset asset) {
        if (asset != null && asset.getBrowserDownloadUrl() != null) {
            return asset.getBrowserDownloadUrl();
        }
        if (release != null) {
            return release.getZipballUrl();
        }
        return null;
    }

    static String obtenerNombreArchivo(GitHubAsset asset, String version) {
        if (asset != null && asset.getName() != null && !asset.getName().isEmpty()) {
            return asset.getName();
        }
        if (version != null) {
            return nombreArchivoPorVersion(version);
        }
        return null;
    }

    static int compararVersiones(String serverVersion, String localVersion) {
        int[] server = parseVersion(serverVersion);
        int[] local = parseVersion(localVersion);
        int max = Math.max(server.length, local.length);
        for (int i = 0; i < max; i++) {
            int sv = i < server.length ? server[i] : 0;
            int lv = i < local.length ? local[i] : 0;
            if (sv != lv) {
                return Integer.compare(sv, lv);
            }
        }
        return 0;
    }

    private static String normalizarVersion(String version) {
        if (version == null) {
            return null;
        }
        String limpia = version.trim();
        if (limpia.toLowerCase(Locale.ROOT).startsWith("v")) {
            limpia = limpia.substring(1);
        }
        return limpia;
    }

    private static int[] parseVersion(String version) {
        if (version == null || version.isEmpty()) {
            return new int[0];
        }
        String[] partes = version.split("\\.");
        int[] valores = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            valores[i] = parseVersionParte(partes[i]);
        }
        return valores;
    }

    private static int parseVersionParte(String parte) {
        StringBuilder digitos = new StringBuilder();
        for (int i = 0; i < parte.length(); i++) {
            char c = parte.charAt(i);
            if (Character.isDigit(c)) {
                digitos.append(c);
            } else if (digitos.length() > 0) {
                break;
            }
        }
        if (digitos.length() == 0) {
            return 0;
        }
        return Integer.parseInt(digitos.toString());
    }

    private static String nombreArchivoPorVersion(String version) {
        if (version == null || version.isEmpty()) {
            return null;
        }
        return Constantes.NOMBRE_APP + "-" + version + ".zip";
    }

    private static GitHubRelease obtenerUltimaRelease() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("User-Agent", Constantes.NOMBRE_APP);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Respuesta API GitHub: " + status + " " + conn.getResponseMessage());
        }
        try (InputStream in = conn.getInputStream()) {
            return MAPPER.readValue(in, GitHubRelease.class);
        }
    }

    private static InputStream abrirStreamDescarga(String urlDescarga) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlDescarga).openConnection();
        conn.setRequestProperty("User-Agent", Constantes.NOMBRE_APP);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Respuesta descarga GitHub: " + status + " " + conn.getResponseMessage());
        }
        return conn.getInputStream();
    }
}
