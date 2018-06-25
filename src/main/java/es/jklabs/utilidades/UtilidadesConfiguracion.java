package es.jklabs.utilidades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.jklabs.json.configuracion.Configuracion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public class UtilidadesConfiguracion {

    private static final String CONFIG_JSON = "config.json";
    private static final Logger LOG = Logger.getLogger();

    private UtilidadesConfiguracion() {

    }

    public static Configuracion loadConfig() {
        File file = new File(CONFIG_JSON);
        if (file.exists()) {
            try {
                UtilidadesFichero.createBaseFolder();
                Files.move(file.toPath(), FileSystems.getDefault().getPath(UtilidadesFichero.HOME +
                        UtilidadesFichero.SEPARADOR + UtilidadesFichero.BEYOND_DEPLOY_FOLDER + UtilidadesFichero
                        .SEPARADOR + CONFIG_JSON));
            } catch (IOException e) {
                LOG.error("Mover archivo de configuracion", e);
            }
        }
        return loadConfig(new File(UtilidadesFichero.HOME + UtilidadesFichero.SEPARADOR +
                UtilidadesFichero.BEYOND_DEPLOY_FOLDER + UtilidadesFichero.SEPARADOR + CONFIG_JSON));
    }

    public static void guardarConfiguracion(Configuracion configuracion) {
        guardarConfiguracion(configuracion, new File(UtilidadesFichero.HOME + UtilidadesFichero.SEPARADOR +
                UtilidadesFichero.BEYOND_DEPLOY_FOLDER + UtilidadesFichero.SEPARADOR + CONFIG_JSON));
    }

    public static void guardarConfiguracion(Configuracion configuracion, File file) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            UtilidadesFichero.createBaseFolder();
            mapper.writeValue(file, configuracion);
        } catch (IOException e) {
            LOG.error("Guardar configuracion", e);
        }
    }

    public static Configuracion loadConfig(File file) {
        ObjectMapper mapper = new ObjectMapper();
        Configuracion configuracion = null;
        try {
            configuracion = mapper.readValue(file, Configuracion.class);
        } catch (FileNotFoundException e) {
            LOG.info("Fichero de configuracion no encontrado", e);
        } catch (IOException e) {
            LOG.error("Error de lectura del fichero de configuracion", e);
        }
        return configuracion;
    }
}
