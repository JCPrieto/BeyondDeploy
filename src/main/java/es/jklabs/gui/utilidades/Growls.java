package es.jklabs.gui.utilidades;

import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.Mensajes;
import javafx.application.Platform;
import org.controlsfx.control.Notifications;

public class Growls {

    private static final Logger LOG = Logger.getLogger();

    private Growls(){

    }

    public static void mostrarError(String cuerpo, Exception e) {
        mostrarError(null, cuerpo, e);
    }

    public static void mostrarInfo(String titulo, String cuerpo) {
        Platform.runLater(() -> getGrowl(titulo, Mensajes.getMensaje(cuerpo))
                .showInformation());
    }

    public static void mostrarError(String titulo, String cuerpo, Exception e) {
        Platform.runLater(() -> getGrowl(titulo, Mensajes.getError(cuerpo))
                .showError());
        LOG.error(cuerpo, e);
    }

    public static void mostrarAviso(String titulo, String cuerpo) {
        Platform.runLater(() -> getGrowl(titulo, Mensajes.getError(cuerpo))
                .showWarning());
    }

    public static void mostrarInfo(String cuerpo) {
        mostrarInfo(null, cuerpo);
    }

    private static Notifications getGrowl(String titulo, String cuerpo) {
        return Notifications.create()
                .darkStyle()
                .title(titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP)
                .text(cuerpo);
    }

}
