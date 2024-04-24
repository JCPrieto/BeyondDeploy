package es.jklabs.utilidades;

import java.text.MessageFormat;
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

    public static String getMensaje(String key, String[] params) {
        String text = getMensaje(key);
        return addParametros(text, params);
    }

    private static String addParametros(String text, String[] params) {
        if (params != null) {
            MessageFormat mf = new MessageFormat(text, Locale.getDefault());
            text = mf.format(params, new StringBuffer(), null).toString();
        }
        return text;
    }

    public static String getError(String key, String[] params) {
        String text = getError(key);
        return addParametros(text, params);
    }
}
