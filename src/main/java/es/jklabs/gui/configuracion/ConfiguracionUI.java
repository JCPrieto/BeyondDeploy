package es.jklabs.gui.configuracion;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.utilidades.UtilidadesConfiguracion;
import es.jklabs.utilidades.UtilidadesEncryptacion;
import es.jklabs.utilidades.UtilidadesString;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class ConfiguracionUI extends JDialog {

    private static final long serialVersionUID = -3135251684578436628L;
    private static final String GUARDAR_CONFIGURACION = "guardar.configuracion";
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private final MainUI padre;
    private Configuracion configuracion;
    private JTextField txBucketName;
    private JTextField txAccesKey;
    private JPasswordField txSecretKey;

    public ConfiguracionUI(MainUI mainUI, Configuracion configuracion) {
        super(mainUI, mensajes.getString("configuracion"), true);
        this.padre = mainUI;
        this.configuracion = configuracion;
        cargarPantalla();
    }

    private void cargarPantalla() {
        this.setLayout(new BorderLayout());
        this.add(cargarPanelCentral(), BorderLayout.CENTER);
        this.add(cargarBotonera(), BorderLayout.SOUTH);
        this.pack();
    }

    private JPanel cargarBotonera() {
        JPanel panel = new JPanel();
        JButton btnAceptar = new JButton(mensajes.getString("aceptar"));
        btnAceptar.addActionListener(al -> guardarConfiguracion());
        panel.add(btnAceptar);
        return panel;
    }

    private void guardarConfiguracion() {
        if (validaFormularioConfiguracion()) {
            guardarConfiguracion2();
            this.dispose();
        }
    }

    private void guardarConfiguracion2() {
        if (configuracion == null) {
            configuracion = new Configuracion();
        }
        if (configuracion.getBucketConfig() == null) {
            configuracion.setBucketConfig(new BucketConfig());
        }
        configuracion.getBucketConfig().setBucketName(txBucketName.getText());
        configuracion.getBucketConfig().setAccesKey(txAccesKey.getText());
        configuracion.getBucketConfig().setSecretKey(UtilidadesEncryptacion.encrypt(String.valueOf(txSecretKey
                .getPassword())));
        UtilidadesConfiguracion.guardarConfiguracion(configuracion);
        padre.actualizarPanelCentral();
    }

    private boolean validaFormularioConfiguracion() {
        boolean valido = true;
        if (UtilidadesString.isEmpty(txBucketName)) {
            valido = false;
            Growls.mostrarAviso(padre, GUARDAR_CONFIGURACION, "nombre.bucket.vacio");
        }
        if (UtilidadesString.isEmpty(txAccesKey)) {
            valido = false;
            Growls.mostrarAviso(padre, GUARDAR_CONFIGURACION, "acces.key.vacio");
        }
        if (UtilidadesString.isEmpty(txSecretKey)) {
            valido = false;
            Growls.mostrarAviso(padre, GUARDAR_CONFIGURACION, "secret.key.vacio");
        }
        return valido;
    }

    private JPanel cargarPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        JLabel lbBucketName = new JLabel(mensajes.getString("nombre.bucket"));
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(lbBucketName, c);
        txBucketName = new JTextField();
        txBucketName.setColumns(10);
        c.gridx = 1;
        c.gridy = 0;
        panel.add(txBucketName, c);
        JLabel lbAccesKey = new JLabel(mensajes.getString("acces.key"));
        c.gridx = 0;
        c.gridy = 1;
        panel.add(lbAccesKey, c);
        txAccesKey = new JTextField();
        txAccesKey.setColumns(10);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(txAccesKey, c);
        JLabel lbSecretKey = new JLabel(mensajes.getString("secret.key"));
        c.gridx = 0;
        c.gridy = 5;
        panel.add(lbSecretKey, c);
        txSecretKey = new JPasswordField();
        txSecretKey.setColumns(10);
        c.gridx = 1;
        c.gridy = 5;
        panel.add(txSecretKey, c);
        cargarDatosFormulario();
        return panel;
    }

    private void cargarDatosFormulario() {
        if (configuracion != null && configuracion.getBucketConfig() != null) {
            txBucketName.setText(configuracion.getBucketConfig().getBucketName());
            txAccesKey.setText(configuracion.getBucketConfig().getAccesKey());
            txSecretKey.setText(UtilidadesEncryptacion.decrypt(configuracion.getBucketConfig().getSecretKey()));
        }
    }
}
