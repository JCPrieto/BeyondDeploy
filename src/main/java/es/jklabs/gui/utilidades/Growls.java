package es.jklabs.gui.utilidades;

import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.Mensajes;
import org.gnome.notify.Notification;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Growls {

    private static final Logger LOG = Logger.getLogger();
    private static TrayIcon trayIcon;

    private Growls(){

    }

    public static void mostrarError(String cuerpo, Exception e) {
        mostrarError(null, cuerpo, e);
    }

    private static void mostrarInfo(String titulo, String cuerpo) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo != null ? Mensajes.getMensaje(titulo) : null, Mensajes.getMensaje(cuerpo), TrayIcon.MessageType.INFO);
        } else {
            new Notification(titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP, Mensajes.getMensaje(cuerpo), "dialog-information").show();
        }
    }

    public static void mostrarError(String titulo, String cuerpo, Exception e) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo != null ? Mensajes.getMensaje(titulo) : null, Mensajes.getError(cuerpo), TrayIcon.MessageType.ERROR);
        } else {
            new Notification(titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP, Mensajes.getError(cuerpo), "dialog-error").show();
        }
        LOG.error(cuerpo, e);
    }

    public static void mostrarAviso(String titulo, String cuerpo) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo != null ? Mensajes.getMensaje(titulo) : null, Mensajes.getError(cuerpo), TrayIcon.MessageType.WARNING);
        } else {
            new Notification(titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP, Mensajes.getError(cuerpo), "dialog-warning").show();
        }
    }

    public static void mostrarInfo(String cuerpo) {
        mostrarInfo(null, cuerpo);
    }

    public static void init() {
        trayIcon = null;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            SystemTray tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(Growls.class.getClassLoader().getResource
                    ("img/icons/s3-bucket.png"))).getImage(), Constantes.NOMBRE_APP);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                LOG.error("establecer.icono.systray", e);
            }
        }
    }
}
