package es.jklabs.gui.utilidades;

import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.Mensajes;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Growls {

    private static final String NOTIFY_SEND = "notify-send";
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
            try {
                Runtime.getRuntime().exec(new String[]{NOTIFY_SEND,
                        titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP,
                        Mensajes.getMensaje(cuerpo),
                        "--icon=dialog-information"});
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    public static void mostrarError(String titulo, String cuerpo, Exception e) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo != null ? Mensajes.getMensaje(titulo) : null, Mensajes.getError(cuerpo), TrayIcon.MessageType.ERROR);
        } else {
            try {
                Runtime.getRuntime().exec(new String[]{NOTIFY_SEND,
                        titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP,
                        Mensajes.getError(cuerpo),
                        "--icon=dialog-error"});
            } catch (IOException e2) {
                Logger.error(e2);
            }
        }
        Logger.error(cuerpo, e);
    }

    public static void mostrarAviso(String titulo, String cuerpo) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo != null ? Mensajes.getMensaje(titulo) : null, Mensajes.getError(cuerpo), TrayIcon.MessageType.WARNING);
        } else {
            try {
                Runtime.getRuntime().exec(new String[]{NOTIFY_SEND,
                        titulo != null ? Mensajes.getMensaje(titulo) : Constantes.NOMBRE_APP,
                        Mensajes.getError(cuerpo),
                        "--icon=dialog-warning"});
            } catch (IOException e) {
                Logger.error(e);
            }
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
                Logger.error("establecer.icono.systray", e);
            }
        }
    }
}
