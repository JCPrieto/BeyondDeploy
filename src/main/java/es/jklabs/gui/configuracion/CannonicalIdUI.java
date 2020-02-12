package es.jklabs.gui.configuracion;

import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.table.UtilidadesTabla;
import es.jklabs.utilidades.Mensajes;
import es.jklabs.utilidades.UtilidadesString;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CannonicalIdUI extends JDialog {
    private final ConfiguracionUI padre;
    private final boolean edit;
    private JTextField txCuentaName;
    private JTextField txCannonicalId;

    public CannonicalIdUI(ConfiguracionUI configuracionUI, boolean edit) {
        super(configuracionUI, Mensajes.getMensaje("cannonical.id"), true);
        this.padre = configuracionUI;
        this.edit = edit;
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
        JButton btnAceptar = new JButton(Mensajes.getMensaje("aceptar"));
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
        if (edit) {
            UtilidadesTabla.actualizarSeleccionado(padre.getTbCannonicalId(), txCuentaName.getText(), txCannonicalId.getText());
        } else {
            padre.getTmCannonicalId().addRow(new Object[]{txCuentaName.getText(), txCannonicalId.getText()});
        }
    }

    private boolean validaFormularioConfiguracion() {
        boolean valido = true;
        if (UtilidadesString.isEmpty(txCuentaName)) {
            valido = false;
            Growls.mostrarAviso("guardar.configuracion", "nombre.cuenta.vacio");
        }
        if (UtilidadesString.isEmpty(txCannonicalId)) {
            valido = false;
            Growls.mostrarAviso("guardar.configuracion", "cannonical.id.vacio");
        }
        return valido;
    }

    private JPanel cargarPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        JLabel lbCuenta = new JLabel(Mensajes.getMensaje("nombre.cuenta"));
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(lbCuenta, c);
        txCuentaName = new JTextField();
        txCuentaName.setColumns(15);
        c.gridx = 1;
        c.gridy = 0;
        panel.add(txCuentaName, c);
        JLabel lbCannonicalId = new JLabel(Mensajes.getMensaje("cannonical.id"));
        c.gridx = 0;
        c.gridy = 1;
        panel.add(lbCannonicalId, c);
        txCannonicalId = new JTextField();
        txCannonicalId.setColumns(15);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(txCannonicalId, c);
        cargarDatosFormulario();
        return panel;
    }

    private void cargarDatosFormulario() {
        if (edit) {
            txCuentaName.setText(UtilidadesTabla.getValorSeleccionado(padre.getTbCannonicalId(), 0));
            txCannonicalId.setText(UtilidadesTabla.getValorSeleccionado(padre.getTbCannonicalId(), 1));
        }
    }
}
