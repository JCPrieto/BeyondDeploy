package es.jklabs.utilidades;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Mensajes {

    private Mensajes() {

    }

    public static String getError(String key) {
        return getResource("i18n/errores", key);
    }

    public static String getMensaje(String key) {
        return getResource("i18n/mensajes", key);
    }

    private static String getResource(String resource, String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(resource, Locale.getDefault());
        String text;
        try {
            text = bundle.getString(key);
        } catch (MissingResourceException e) {
            text = key;
        }
        return text;
    }

}
