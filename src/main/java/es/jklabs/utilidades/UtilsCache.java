package es.jklabs.utilidades;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.jklabs.json.configuracion.Cache;

import java.io.File;
import java.io.IOException;

public class UtilsCache {

    private static final String CACHE = "cache";

    public static String getLastUploadFolder() {
        File cacheFile = new File(UtilidadesFichero.HOME + UtilidadesFichero.SEPARADOR +
                UtilidadesFichero.BEYOND_DEPLOY_FOLDER + UtilidadesFichero.SEPARADOR + CACHE);
        if (cacheFile.exists()) {
            ObjectMapper mapper = getObjectMapper();
            Cache cache;
            try {
                cache = mapper.readValue(cacheFile, Cache.class);
                return cache.getUploadFolder();
            } catch (IOException e) {
                Logger.error("access.cache.file", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static void setLastUploadFolder(String path) {
        File cacheFile = new File(UtilidadesFichero.HOME + UtilidadesFichero.SEPARADOR +
                UtilidadesFichero.BEYOND_DEPLOY_FOLDER + UtilidadesFichero.SEPARADOR + CACHE);
        ObjectMapper mapper = getObjectMapper();
        Cache cache;
        try {
            if (cacheFile.exists()) {
                cache = mapper.readValue(cacheFile, Cache.class);
            } else {
                cache = new Cache();
            }
            cache.setUploadFolder(path);
            mapper.writeValue(cacheFile, cache);
        } catch (IOException e) {
            Logger.error("access.cache.file", e);
        }
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
