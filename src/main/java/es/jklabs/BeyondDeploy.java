package es.jklabs;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.UtilidadesConfiguracion;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;

import javax.swing.*;

public class BeyondDeploy {

    private static final Logger LOG = Logger.getLogger();

    public static void main(String[] args) {
        Logger.eliminarLogsVacios();
        final JFXPanel fxPanel = new JFXPanel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Configuracion configuracion = UtilidadesConfiguracion.loadConfig();
            if (configuracion == null) {
                configuracion = new Configuracion();
                MainUI mainUI = new MainUI(fxPanel, configuracion);
                ConfiguracionUI configuracionUI = new ConfiguracionUI(mainUI, configuracion);
                configuracionUI.setVisible(true);
                mainUI.setVisible(true);
            } else {
                MainUI mainUI = new MainUI(fxPanel, configuracion);
                mainUI.setVisible(true);
            }
            Platform.runLater(() -> initFX(fxPanel));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            LOG.error("Cargar el LookAndFeel del S.O", e);
        }
    }

    private static void initFX(JFXPanel fxPanel) {
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private static Scene createScene() {
        Group root = new Group();
        return new Scene(root);
    }

}
