package es.jklabs.utilidades;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

class UtilidadesFichero {

    static final String BEYOND_DEPLOY_FOLDER = ".BeyondDeploy";
    static final String SEPARADOR = System.getProperty("file.separator");
    static final String HOME = System.getProperty("user.home");

    private UtilidadesFichero() {

    }

    static void createBaseFolder() {
        File base = new File(HOME + SEPARADOR + BEYOND_DEPLOY_FOLDER);
        if (!base.exists()) {
            try {
                Files.createDirectory(FileSystems.getDefault().getPath(HOME + SEPARADOR + BEYOND_DEPLOY_FOLDER));
            } catch (IOException e) {
                Logger.error("Crear carpeta base", e);
            }
        }
    }
}
