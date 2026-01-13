package es.jklabs;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.UtilidadesConfiguracion;

import javax.swing.*;

public class BeyondDeploy {

    public static void main(String[] args) {
        System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
        Logger.eliminarLogsVacios();
        Logger.init();
        SwingUtilities.invokeLater(() -> {
            try {
                Growls.init();
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException e) {
                Logger.error("Cargar el LookAndFeel del S.O", e);
            }
            new SwingWorker<Configuracion, Void>() {
                @Override
                protected Configuracion doInBackground() {
                    return UtilidadesConfiguracion.loadConfig();
                }

                @Override
                protected void done() {
                    try {
                        Configuracion configuracion = get();
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
                    } catch (Exception e) {
                        Logger.error("Cargar configuracion", e);
                    }
                }
            }.execute();
        });
    }

}
