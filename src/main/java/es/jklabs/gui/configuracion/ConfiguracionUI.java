package es.jklabs.gui.configuracion;

import es.jklabs.gui.MainUI;
import es.jklabs.json.configuracion.Configuracion;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class ConfiguracionUI extends JDialog {

    private static final long serialVersionUID = -3135251684578436628L;
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private final MainUI padre;
    private final Configuracion configuracion;

    public ConfiguracionUI(MainUI mainUI, Configuracion configuracion) {
        super(mainUI, mensajes.getString("servidores"), true);
        this.padre = mainUI;
        this.configuracion = configuracion;
        cargarPantalla();
    }

    private void cargarPantalla() {

    }
}
