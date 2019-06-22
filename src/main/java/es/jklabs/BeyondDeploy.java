package es.jklabs;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.UtilidadesConfiguracion;
import org.gnome.gtk.Gtk;
import org.gnome.notify.Notify;

import javax.swing.*;

public class BeyondDeploy {

    private static final Logger LOG = Logger.getLogger();

    public static void main(String[] args) {
        try {
            Gtk.init(args);
            Notify.init(Constantes.NOMBRE_APP);
            Growls.init(true);
        } catch (Exception | UnsatisfiedLinkError e) {
            Growls.init(false);
        }
        Logger.eliminarLogsVacios();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Configuracion configuracion = UtilidadesConfiguracion.loadConfig();
            if (configuracion == null) {
                configuracion = new Configuracion();
                MainUI mainUI = new MainUI(configuracion);
                ConfiguracionUI configuracionUI = new ConfiguracionUI(mainUI, configuracion);
                configuracionUI.setVisible(true);
                mainUI.setVisible(true);
            } else {
                MainUI mainUI = new MainUI(configuracion);
                mainUI.setVisible(true);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            LOG.error("Cargar el LookAndFeel del S.O", e);
        }
    }

}
