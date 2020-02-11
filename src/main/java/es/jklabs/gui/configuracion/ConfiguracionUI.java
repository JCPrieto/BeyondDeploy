package es.jklabs.gui.configuracion;

import com.amazonaws.regions.Regions;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.UtilidadesImagenes;
import es.jklabs.gui.utilidades.table.model.CannonicalTableModel;
import es.jklabs.json.configuracion.BucketConfig;
import es.jklabs.json.configuracion.CannonicalId;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.utilidades.Mensajes;
import es.jklabs.utilidades.UtilidadesConfiguracion;
import es.jklabs.utilidades.UtilidadesEncryptacion;
import es.jklabs.utilidades.UtilidadesString;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
    private JTable tbCannonicalId;
    private CannonicalTableModel tmCannonicalId;
    private JComboBox<Regions> cbRegion;

    public ConfiguracionUI(MainUI mainUI, Configuracion configuracion) {
        super(mainUI, mensajes.getString("configuracion"), true);
        this.padre = mainUI;
        this.configuracion = configuracion;
        setPreferredSize(new Dimension(600, 350));
        cargarPantalla();
    }

    private void cargarPantalla() {
        this.setLayout(new BorderLayout());
        this.add(cargarPanelNorte(), BorderLayout.NORTH);
        this.add(cargarPanelCentral(), BorderLayout.CENTER);
        this.add(cargarBotonera(), BorderLayout.SOUTH);
        this.pack();
    }

    private JPanel cargarPanelCentral() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(cargarBotonesTabla(), BorderLayout.NORTH);
        panel.add(cargarTabla(), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane cargarTabla() {
        tbCannonicalId = new JTable();
        tmCannonicalId = new CannonicalTableModel(configuracion.getCannonicalIds());
        tbCannonicalId.setModel(tmCannonicalId);
        tbCannonicalId.setFillsViewportHeight(true);
        tbCannonicalId.setAutoCreateRowSorter(true);
        tbCannonicalId.getColumnModel().getColumn(0).setPreferredWidth(150);
        tbCannonicalId.getColumnModel().getColumn(1).setPreferredWidth(450);
        return new JScrollPane(tbCannonicalId);
    }

    private JPanel cargarBotonesTabla() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddCannonical = new JButton(UtilidadesImagenes.getIcono("plus.png"));
        btnAddCannonical.setPreferredSize(new Dimension(30, 30));
        btnAddCannonical.setToolTipText(Mensajes.getMensaje("anadir"));
        btnAddCannonical.addActionListener(l -> addCannonicalId());
        JButton btnEditCannonical = new JButton(UtilidadesImagenes.getIcono("edit.png"));
        btnEditCannonical.setPreferredSize(new Dimension(30, 30));
        btnEditCannonical.setToolTipText(Mensajes.getMensaje("editar"));
        btnEditCannonical.addActionListener(l -> editCannonicalId());
        JButton btnRemoveCannonical = new JButton(UtilidadesImagenes.getIcono("trash.png"));
        btnRemoveCannonical.setPreferredSize(new Dimension(30, 30));
        btnRemoveCannonical.setToolTipText(Mensajes.getMensaje("eliminar"));
        btnRemoveCannonical.addActionListener(l -> removeCannonicalId());
        panel.add(btnAddCannonical);
        panel.add(btnEditCannonical);
        panel.add(btnRemoveCannonical);
        return panel;
    }

    private void removeCannonicalId() {
        if (tbCannonicalId.getSelectedRow() > -1) {
            tmCannonicalId.removeRow(tbCannonicalId.convertRowIndexToModel(tbCannonicalId.getSelectedRow()));
            tbCannonicalId.clearSelection();
        } else {
            Growls.mostrarInfo(Mensajes.getError("elemento.seleccionado"));
        }
    }

    private void editCannonicalId() {
        if (tbCannonicalId.getSelectedRow() > -1) {
            mostrarDialogoCannonicalId(true);
        } else {
            Growls.mostrarInfo(Mensajes.getError("elemento.seleccionado"));
        }
    }

    private void mostrarDialogoCannonicalId(boolean edit) {
        CannonicalIdUI cannonicalIdUI = new CannonicalIdUI(this, edit);
        cannonicalIdUI.setVisible(true);
    }

    private void addCannonicalId() {
        mostrarDialogoCannonicalId(false);
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
        if (configuracion.getCannonicalIds() == null) {
            configuracion.setCannonicalIds(new ArrayList<>());
        }
        configuracion.getBucketConfig().setBucketName(txBucketName.getText());
        configuracion.getBucketConfig().setAccesKey(txAccesKey.getText());
        configuracion.getBucketConfig().setSecretKey(UtilidadesEncryptacion.encrypt(String.valueOf(txSecretKey
                .getPassword())));
        List<CannonicalId> list = new ArrayList<>();
        for (int i = 0; i < tmCannonicalId.getRowCount(); i++) {
            CannonicalId cannonicalId = new CannonicalId();
            cannonicalId.setNombre(String.valueOf(tmCannonicalId.getValueAt(i, 0)));
            cannonicalId.setId(String.valueOf(tmCannonicalId.getValueAt(i, 1)));
            list.add(cannonicalId);
        }
        configuracion.getCannonicalIds().removeIf(c -> !list.contains(c));
        list.stream().filter(c ->
                configuracion.getCannonicalIds().contains(c)).forEach(c ->
                configuracion.getCannonicalIds().get(configuracion.getCannonicalIds().indexOf(c)).setNombre(c.getNombre()));
        list.stream().filter(c ->
                !configuracion.getCannonicalIds().contains(c)).forEach(c ->
                configuracion.getCannonicalIds().add(c));
        UtilidadesConfiguracion.guardarConfiguracion(configuracion);
        padre.actualizarPanelCentral();
    }

    private boolean validaFormularioConfiguracion() {
        boolean valido = true;
        if (UtilidadesString.isEmpty(txBucketName)) {
            valido = false;
            Growls.mostrarAviso(GUARDAR_CONFIGURACION, "nombre.bucket.vacio");
        }
        if (UtilidadesString.isEmpty(txAccesKey)) {
            valido = false;
            Growls.mostrarAviso(GUARDAR_CONFIGURACION, "acces.key.vacio");
        }
        if (UtilidadesString.isEmpty(txSecretKey)) {
            valido = false;
            Growls.mostrarAviso(GUARDAR_CONFIGURACION, "secret.key.vacio");
        }
        if (cbRegion.getSelectedItem() == null) {
            valido = false;
            Growls.mostrarAviso(GUARDAR_CONFIGURACION, "region.vacio");
        }
        return valido;
    }

    private JPanel cargarPanelNorte() {
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
        txBucketName.setColumns(15);
        c.gridx = 1;
        c.gridy = 0;
        panel.add(txBucketName, c);
        JLabel lbAccesKey = new JLabel(mensajes.getString("acces.key"));
        c.gridx = 0;
        c.gridy = 1;
        panel.add(lbAccesKey, c);
        txAccesKey = new JTextField();
        txAccesKey.setColumns(15);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(txAccesKey, c);
        JLabel lbSecretKey = new JLabel(mensajes.getString("secret.key"));
        c.gridx = 0;
        c.gridy = 5;
        panel.add(lbSecretKey, c);
        txSecretKey = new JPasswordField();
        txSecretKey.setColumns(15);
        c.gridx = 1;
        c.gridy = 5;
        panel.add(txSecretKey, c);
        JLabel lbRegion = new JLabel(Mensajes.getMensaje("region"));
        c.gridx = 0;
        c.gridy = 6;
        panel.add(lbRegion, c);
        cbRegion = new JComboBox<>(Regions.values());
        c.gridx = 1;
        c.gridy = 6;
        panel.add(cbRegion, c);
        cargarDatosFormulario();
        return panel;
    }

    private void cargarDatosFormulario() {
        if (configuracion != null && configuracion.getBucketConfig() != null) {
            txBucketName.setText(configuracion.getBucketConfig().getBucketName());
            txAccesKey.setText(configuracion.getBucketConfig().getAccesKey());
            txSecretKey.setText(UtilidadesEncryptacion.decrypt(configuracion.getBucketConfig().getSecretKey()));
            cbRegion.setSelectedItem(configuracion.getBucketConfig().getRegion());
        }
    }

    public JTable getTbCannonicalId() {
        return tbCannonicalId;
    }

    public CannonicalTableModel getTmCannonicalId() {
        return tmCannonicalId;
    }
}
