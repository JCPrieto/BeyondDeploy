package es.jklabs.utilidades;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

class UtilidadesFichero {

    static final String BEYOND_DEPLOY_FOLDER = ".BeyondDeploy";
    static final String SEPARADOR = System.getProperty("file.separator");
    static String HOME = System.getProperty("user.home");
    private static final Logger LOG = Logger.getLogger();

    private UtilidadesFichero() {

    }

    static void createBaseFolder() {
        File base = new File(HOME + SEPARADOR + BEYOND_DEPLOY_FOLDER);
        if (!base.exists()) {
            try {
                Files.createDirectory(FileSystems.getDefault().getPath(HOME + SEPARADOR + BEYOND_DEPLOY_FOLDER));
            } catch (IOException e) {
                LOG.error("Crear carpeta base", e);
            }
        }
    }
}
