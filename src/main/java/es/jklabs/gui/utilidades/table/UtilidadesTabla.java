package es.jklabs.gui.utilidades.table;

import javax.swing.*;

public class UtilidadesTabla {

    private UtilidadesTabla() {

    }


    public static String getValorSeleccionado(JTable jTable, int column) {
        return String.valueOf(jTable.getModel().getValueAt(jTable.convertRowIndexToModel(jTable.getSelectedRow()), column));
    }

    public static void actualizarSeleccionado(JTable jTable, String... values) {
        for (int i = 0; i < values.length; i++) {
            jTable.getModel().setValueAt(values[i], jTable.convertRowIndexToModel(jTable.getSelectedRow()), i);
        }
    }
}
