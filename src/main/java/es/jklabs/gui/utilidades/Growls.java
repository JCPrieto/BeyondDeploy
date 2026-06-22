package es.jklabs.gui.utilidades;

import com.sshtools.twoslices.*;
import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.Mensajes;

import java.net.URL;

public class Growls {

    private static final int TIMEOUT_SECONDS = 5;

    private Growls() {

    }

    public static void mostrarError(String cuerpo, Exception e) {
        mostrarError(null, cuerpo, e);
    }

    private static void mostrarInfo(String titulo, String cuerpo) {
        mostrarNotificacion(ToastType.INFO, titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP,
                Mensajes.getMensaje(cuerpo));
    }

    public static void mostrarError(String titulo, String cuerpo, Exception e) {
        mostrarNotificacion(ToastType.ERROR, titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP,
                Mensajes.getError(cuerpo));
        Logger.error(cuerpo, e);
    }

    public static void mostrarAviso(String titulo, String cuerpo) {
        mostrarAviso(titulo, cuerpo, null);
    }

    public static void mostrarAviso(String titulo, String cuerpo, String[] params) {
        mostrarNotificacion(ToastType.WARNING, titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP,
                Mensajes.getError(cuerpo, params));
    }

    public static void mostrarInfo(String cuerpo) {
        mostrarInfo(null, cuerpo);
    }

    public static void init() {
        URL icon = Growls.class.getClassLoader().getResource("img/icons/s3-bucket.png");
        ToasterSettings settings = new ToasterSettings()
                .setAppName(Constantes.NOMBRE_APP)
                .setTimeout(TIMEOUT_SECONDS);
        if (icon != null) {
            settings.setDefaultImage(icon);
        }
        ToasterFactory.setSettings(settings);
    }

    private static void mostrarNotificacion(ToastType tipo, String titulo, String cuerpo) {
        try {
            Toast.builder()
                    .type(tipo)
                    .title(titulo)
                    .content(cuerpo)
                    .timeout(TIMEOUT_SECONDS)
                    .toast();
        } catch (ToasterException | IllegalStateException e) {
            Logger.error("mostrar.notificacion", e);
        }
    }
}
